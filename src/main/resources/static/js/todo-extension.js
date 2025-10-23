/**
 * TodoExtensionManager - 處理待辦事項延期功能的前端邏輯
 */
class TodoExtensionManager {
    constructor() {
        this.currentTodoId = null;
        this.currentDueDate = null;
        this.initEventListeners();
    }
    
    /**
     * 初始化事件監聽器
     */
    initEventListeners() {
        // 延期按鈕點擊事件
        document.addEventListener('click', (e) => {
            if (e.target.closest('.extend-btn')) {
                const button = e.target.closest('.extend-btn');
                this.openExtensionModal(
                    button.getAttribute('data-todo-id'),
                    button.getAttribute('data-todo-title'),
                    button.getAttribute('data-current-due-date')
                );
            }
        });
        
        // 快速選擇按鈕
        document.addEventListener('click', (e) => {
            if (e.target.classList.contains('quick-select')) {
                const days = e.target.getAttribute('data-days');
                document.getElementById('extensionDays').value = days;
                
                // 更新按鈕狀態
                document.querySelectorAll('.quick-select').forEach(btn => {
                    btn.classList.remove('active');
                });
                e.target.classList.add('active');
                
                this.updatePreview();
            }
        });
        
        // 延期天數輸入變更
        const extensionDaysInput = document.getElementById('extensionDays');
        if (extensionDaysInput) {
            extensionDaysInput.addEventListener('input', () => {
                // 清除快速選擇按鈕的活動狀態
                document.querySelectorAll('.quick-select').forEach(btn => {
                    btn.classList.remove('active');
                });
                this.updatePreview();
            });
        }
        
        // 確認延期按鈕
        const confirmExtendBtn = document.getElementById('confirmExtend');
        if (confirmExtendBtn) {
            confirmExtendBtn.addEventListener('click', () => {
                this.confirmExtension();
            });
        }
        
        // 模態框關閉時重置狀態
        const extendModal = document.getElementById('extendModal');
        if (extendModal) {
            extendModal.addEventListener('hidden.bs.modal', () => {
                this.resetModal();
            });
        }
    }
    
    /**
     * 開啟延期模態框
     */
    openExtensionModal(todoId, title, currentDueDate) {
        this.currentTodoId = todoId;
        this.currentDueDate = currentDueDate;
        
        // 設定模態框內容
        const todoTitleSpan = document.getElementById('todoTitle');
        const currentDueDateSpan = document.getElementById('currentDueDate');
        const extendModal = document.getElementById('extendModal');
        
        if (todoTitleSpan) {
            todoTitleSpan.textContent = title;
        }
        
        if (currentDueDateSpan) {
            currentDueDateSpan.textContent = this.formatDate(currentDueDate);
        }
        
        // 重置表單
        this.resetModal();
        
        // 顯示模態框
        if (extendModal) {
            const modal = new bootstrap.Modal(extendModal);
            modal.show();
        } else {
            console.error('Extend modal not found!');
        }
    }
    
    /**
     * 重置模態框狀態
     */
    resetModal() {
        const extensionDaysInput = document.getElementById('extensionDays');
        const newDueDateSpan = document.getElementById('newDueDate');
        const datePreviewHelp = document.getElementById('datePreviewHelp');
        
        if (extensionDaysInput) {
            extensionDaysInput.value = '';
            extensionDaysInput.classList.remove('is-invalid');
        }
        
        if (newDueDateSpan) {
            newDueDateSpan.textContent = '';
        }
        
        if (datePreviewHelp) {
            datePreviewHelp.style.display = 'none';
        }
        
        // 清除快速選擇按鈕狀態
        document.querySelectorAll('.quick-select').forEach(btn => {
            btn.classList.remove('active');
        });
        
        // 隱藏錯誤訊息
        this.clearError();
    }
    
    /**
     * 更新延期預覽
     */
    updatePreview() {
        const days = parseInt(document.getElementById('extensionDays').value);
        
        if (!days || days <= 0) {
            document.getElementById('newDueDate').textContent = '';
            document.getElementById('datePreviewHelp').style.display = 'none';
            return;
        }
        
        // 發送 AJAX 請求取得預覽
        fetch(`/todos/${this.currentTodoId}/extend/preview?days=${days}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            }
        })
        .then(response => {
            if (!response.ok) {
                return response.json().then(err => Promise.reject(err));
            }
            return response.json();
        })
        .then(data => {
            document.getElementById('newDueDate').textContent = this.formatDate(data.newDueDate);
            document.getElementById('datePreviewHelp').style.display = 'block';
            this.clearError();
        })
        .catch(error => {
            const errorMessage = error.error || '預覽失敗，請檢查輸入的天數';
            this.showError(errorMessage);
            document.getElementById('newDueDate').textContent = '';
            document.getElementById('datePreviewHelp').style.display = 'none';
        });
    }
    
    /**
     * 確認延期操作
     */
    confirmExtension() {
        const days = parseInt(document.getElementById('extensionDays').value);
        
        if (!this.validateInput(days)) {
            return;
        }
        
        const requestData = {
            todoId: parseInt(this.currentTodoId),
            extensionDays: days
        };
        
        // 禁用確認按鈕防止重複提交
        const confirmBtn = document.getElementById('confirmExtend');
        const originalText = confirmBtn.innerHTML;
        confirmBtn.disabled = true;
        confirmBtn.innerHTML = '<i class="bi bi-hourglass-split me-1"></i>處理中...';
        
        // 獲取 CSRF token
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
        
        const headers = {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        };
        
        if (csrfToken && csrfHeader) {
            headers[csrfHeader] = csrfToken;
        }
        
        // 發送延期請求
        fetch(`/todos/${this.currentTodoId}/extend`, {
            method: 'POST',
            headers: headers,
            body: JSON.stringify(requestData)
        })
        .then(response => {
            if (!response.ok) {
                return response.json().then(err => Promise.reject(err));
            }
            return response.json();
        })
        .then(response => {
            if (response.success) {
                // 關閉模態框
                const modal = bootstrap.Modal.getInstance(document.getElementById('extendModal'));
                modal.hide();
                
                // 動態更新頁面上的到期日期
                this.updateTodoItemOnPage(response);
                
                // 顯示成功訊息
                this.showSuccessMessage('延期成功！到期日已更新');
            } else {
                this.showError(response.message || '延期失敗');
            }
        })
        .catch(error => {
            const errorMessage = error.message || '延期失敗，請稍後再試';
            this.showError(errorMessage);
        })
        .finally(() => {
            // 恢復確認按鈕狀態
            confirmBtn.disabled = false;
            confirmBtn.innerHTML = originalText;
        });
    }
    
    /**
     * 驗證輸入
     */
    validateInput(days) {
        const extensionDaysInput = document.getElementById('extensionDays');
        
        // 清除之前的驗證狀態
        extensionDaysInput.classList.remove('is-invalid');
        this.clearError();
        
        if (!days || isNaN(days)) {
            this.showError('請輸入有效的延期天數');
            extensionDaysInput.classList.add('is-invalid');
            return false;
        }
        
        if (days <= 0) {
            this.showError('延期天數必須為正數');
            extensionDaysInput.classList.add('is-invalid');
            return false;
        }
        
        if (days > 365) {
            this.showError('延期天數不能超過365天');
            extensionDaysInput.classList.add('is-invalid');
            return false;
        }
        
        return true;
    }
    
    /**
     * 顯示錯誤訊息
     */
    showError(message) {
        const errorDiv = document.getElementById('errorMessage');
        const errorText = document.getElementById('errorText');
        
        if (errorDiv && errorText) {
            errorText.textContent = message;
            errorDiv.style.display = 'block';
            
            // 滾動到錯誤訊息位置
            errorDiv.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
        } else {
            console.error('Error elements not found:', { errorDiv, errorText });
            // 如果找不到錯誤元素，使用 alert 作為備用方案
            alert(message);
        }
    }
    
    /**
     * 清除錯誤訊息
     */
    clearError() {
        const errorDiv = document.getElementById('errorMessage');
        if (errorDiv) {
            errorDiv.style.display = 'none';
        }
    }
    
    /**
     * 顯示成功訊息
     */
    showSuccessMessage(message) {
        // 創建成功提示
        const alertDiv = document.createElement('div');
        alertDiv.className = 'alert alert-success alert-dismissible fade show position-fixed';
        alertDiv.style.cssText = 'top: 20px; right: 20px; z-index: 9999; min-width: 300px;';
        alertDiv.innerHTML = `
            <i class="bi bi-check-circle me-2"></i>
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        `;
        
        document.body.appendChild(alertDiv);
        
        // 自動移除提示
        setTimeout(() => {
            if (alertDiv.parentNode) {
                alertDiv.parentNode.removeChild(alertDiv);
            }
        }, 3000);
    }
    
    /**
     * 動態更新頁面上的待辦事項信息
     */
    updateTodoItemOnPage(response) {
        // 找到對應的待辦事項卡片
        const todoCard = document.querySelector(`[data-todo-id="${this.currentTodoId}"]`)?.closest('.todo-item');
        
        if (!todoCard) {
            console.warn('找不到待辦事項卡片，將重新載入頁面');
            setTimeout(() => window.location.reload(), 1000);
            return;
        }
        
        // 更新到期日顯示
        const dueDateSpan = todoCard.querySelector('.due-date-display');
        if (dueDateSpan) {
            dueDateSpan.textContent = this.formatDate(response.newDueDate);
        }
        
        // 更新延期按鈕的 data 屬性
        const extendBtn = todoCard.querySelector('.extend-btn');
        if (extendBtn) {
            extendBtn.setAttribute('data-current-due-date', response.newDueDate);
            
            // 檢查新的到期日是否還符合延期條件（三天內）
            const newDueDate = new Date(response.newDueDate + 'T00:00:00');
            const today = new Date();
            today.setHours(0, 0, 0, 0); // 設定為當天開始
            const threeDaysLater = new Date(today.getTime() + (3 * 24 * 60 * 60 * 1000));
            
            // 如果新到期日超過三天，隱藏延期按鈕
            if (newDueDate > threeDaysLater) {
                extendBtn.style.display = 'none';
            }
        }
        
        // 更新或移除警告標籤
        this.updateWarningBadges(todoCard, response.newDueDate);
        
        // 添加視覺反饋效果
        this.addUpdateAnimation(todoCard);
    }
    
    /**
     * 更新警告標籤（逾期、今日到期等）
     */
    updateWarningBadges(todoCard, newDueDate) {
        // 移除現有的警告標籤
        const existingBadges = todoCard.querySelectorAll('.badge');
        existingBadges.forEach(badge => {
            if (badge.textContent.includes('已逾期') || badge.textContent.includes('今日到期')) {
                badge.parentElement.remove();
            }
        });
        
        // 檢查是否需要添加新的警告標籤
        const today = new Date();
        const dueDate = new Date(newDueDate + 'T00:00:00');
        const todayStr = today.toDateString();
        const dueDateStr = dueDate.toDateString();
        
        const timeInfoDiv = todoCard.querySelector('.d-flex.flex-wrap.gap-3');
        
        if (dueDate < today && todayStr !== dueDateStr) {
            // 已逾期
            const overdueDiv = document.createElement('div');
            overdueDiv.className = 'mt-2';
            overdueDiv.innerHTML = `
                <span class="badge bg-danger">
                    <i class="bi bi-exclamation-triangle me-1"></i>已逾期
                </span>
            `;
            timeInfoDiv.parentElement.appendChild(overdueDiv);
        } else if (todayStr === dueDateStr) {
            // 今日到期
            const todayDueDiv = document.createElement('div');
            todayDueDiv.className = 'mt-2';
            todayDueDiv.innerHTML = `
                <span class="badge bg-warning text-dark">
                    <i class="bi bi-clock me-1"></i>今日到期
                </span>
            `;
            timeInfoDiv.parentElement.appendChild(todayDueDiv);
        }
    }
    
    /**
     * 添加更新動畫效果
     */
    addUpdateAnimation(todoCard) {
        // 添加更新效果
        todoCard.style.transition = 'all 0.3s ease';
        todoCard.style.backgroundColor = '#e8f5e8';
        todoCard.style.transform = 'scale(1.02)';
        
        // 恢復原狀
        setTimeout(() => {
            todoCard.style.backgroundColor = '';
            todoCard.style.transform = '';
        }, 1000);
        
        // 清除 transition
        setTimeout(() => {
            todoCard.style.transition = '';
        }, 1300);
    }
    
    /**
     * 格式化日期顯示
     */
    formatDate(dateString) {
        try {
            const date = new Date(dateString + 'T00:00:00');
            return date.toLocaleDateString('zh-TW', {
                year: 'numeric',
                month: '2-digit',
                day: '2-digit'
            });
        } catch (error) {
            return dateString;
        }
    }
}

// 當 DOM 載入完成後初始化延期管理器
document.addEventListener('DOMContentLoaded', function() {
    // 只在待辦事項列表頁面初始化
    const extendModal = document.getElementById('extendModal');
    if (extendModal) {
        new TodoExtensionManager();
    }
});