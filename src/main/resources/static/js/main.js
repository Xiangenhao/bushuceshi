/**
 * 购物商城前端主要JavaScript文件
 */
document.addEventListener('DOMContentLoaded', function() {
    // 初始化Bootstrap工具提示
    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
    
    // 检查用户登录状态
    checkLoginStatus();
    
    // 更新购物车数量
    updateCartCount();
    
    // 绑定加入购物车点击事件
    bindAddToCartEvents();
    
    // 绑定收藏点击事件
    bindAddToFavoriteEvents();
    
    // 绑定退出登录事件
    bindLogoutEvent();
});

/**
 * 检查用户登录状态
 */
function checkLoginStatus() {
    // 从localStorage获取token
    const token = localStorage.getItem('token');
    
    if (token) {
        // 已登录状态
        document.querySelectorAll('.login-item, .register-item').forEach(item => {
            item.style.display = 'none';
        });
        
        document.querySelectorAll('.logged-in-item').forEach(item => {
            item.style.display = 'block';
        });
        
        // 获取并显示用户名
        const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
        if (userInfo.nickname) {
            document.getElementById('userMenuButton').innerHTML = `
                <i class="bi bi-person-circle"></i> ${userInfo.nickname}
            `;
        }
    } else {
        // 未登录状态
        document.querySelectorAll('.login-item, .register-item').forEach(item => {
            item.style.display = 'block';
        });
        
        document.querySelectorAll('.logged-in-item').forEach(item => {
            item.style.display = 'none';
        });
    }
}

/**
 * 更新购物车数量
 */
function updateCartCount() {
    // 从localStorage获取购物车数据
    const cart = JSON.parse(localStorage.getItem('cart') || '[]');
    const cartCount = cart.length;
    
    // 更新购物车数量显示
    const cartCountElement = document.getElementById('cartCount');
    if (cartCountElement) {
        cartCountElement.textContent = cartCount;
        
        // 如果购物车为空，隐藏徽章
        if (cartCount === 0) {
            cartCountElement.style.display = 'none';
        } else {
            cartCountElement.style.display = 'inline-block';
        }
    }
}

/**
 * 绑定加入购物车点击事件
 */
function bindAddToCartEvents() {
    document.querySelectorAll('.add-to-cart').forEach(button => {
        button.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            
            // 获取商品信息
            const productCard = this.closest('.product-card');
            const productId = productCard.dataset.productId || '1'; // 默认ID为1
            const productName = productCard.querySelector('.product-name').textContent;
            const productPrice = productCard.querySelector('.current-price').textContent.replace('¥', '');
            const productImage = productCard.querySelector('img').src;
            
            // 从localStorage获取购物车数据
            let cart = JSON.parse(localStorage.getItem('cart') || '[]');
            
            // 检查商品是否已在购物车中
            const existingItemIndex = cart.findIndex(item => item.productId === productId);
            
            if (existingItemIndex !== -1) {
                // 如果商品已存在，增加数量
                cart[existingItemIndex].quantity += 1;
            } else {
                // 如果商品不存在，添加新商品
                cart.push({
                    productId: productId,
                    name: productName,
                    price: parseFloat(productPrice),
                    image: productImage,
                    quantity: 1,
                    selected: true
                });
            }
            
            // 保存购物车数据
            localStorage.setItem('cart', JSON.stringify(cart));
            
            // 更新购物车数量
            updateCartCount();
            
            // 显示提示信息
            showToast('已添加到购物车');
        });
    });
}

/**
 * 绑定收藏点击事件
 */
function bindAddToFavoriteEvents() {
    document.querySelectorAll('.add-to-favorite').forEach(button => {
        button.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            
            // 获取商品信息
            const productCard = this.closest('.product-card');
            const productId = productCard.dataset.productId || '1'; // 默认ID为1
            
            // 检查用户是否登录
            const token = localStorage.getItem('token');
            if (!token) {
                // 未登录时跳转到登录页面
                window.location.href = '/login?redirect=' + encodeURIComponent(window.location.href);
                return;
            }
            
            // 切换按钮样式
            this.classList.toggle('btn-outline-danger');
            this.classList.toggle('btn-danger');
            
            if (this.classList.contains('btn-danger')) {
                this.innerHTML = '<i class="bi bi-heart-fill"></i>';
                showToast('已添加到收藏');
            } else {
                this.innerHTML = '<i class="bi bi-heart"></i>';
                showToast('已取消收藏');
            }
            
            // TODO: 发送请求到后端添加/移除收藏
        });
    });
}

/**
 * 绑定退出登录事件
 */
function bindLogoutEvent() {
    const logoutButton = document.getElementById('logoutButton');
    if (logoutButton) {
        logoutButton.addEventListener('click', function(e) {
            e.preventDefault();
            
            // 清除本地存储的令牌和用户信息
            localStorage.removeItem('token');
            localStorage.removeItem('userInfo');
            
            // 更新UI
            checkLoginStatus();
            
            // 显示提示信息
            showToast('已退出登录');
            
            // 延迟跳转到首页
            setTimeout(() => {
                window.location.href = '/';
            }, 1000);
        });
    }
}

/**
 * 显示提示信息
 * @param {string} message 提示消息
 */
function showToast(message) {
    // 检查是否已存在Toast元素
    let toastContainer = document.querySelector('.toast-container');
    
    // 如果不存在，创建一个
    if (!toastContainer) {
        toastContainer = document.createElement('div');
        toastContainer.className = 'toast-container position-fixed bottom-0 end-0 p-3';
        document.body.appendChild(toastContainer);
    }
    
    // 创建Toast元素
    const toastId = 'toast-' + new Date().getTime();
    const toastHtml = `
        <div id="${toastId}" class="toast" role="alert" aria-live="assertive" aria-atomic="true">
            <div class="toast-body">
                ${message}
            </div>
        </div>
    `;
    
    // 添加Toast到容器
    toastContainer.innerHTML += toastHtml;
    
    // 实例化并显示Toast
    const toastElement = document.getElementById(toastId);
    const toast = new bootstrap.Toast(toastElement, { delay: 2000 });
    toast.show();
    
    // 自动移除Toast元素
    toastElement.addEventListener('hidden.bs.toast', function() {
        this.remove();
    });
}

/**
 * 商品卡片点击事件处理
 */
document.addEventListener('click', function(e) {
    // 检查点击的是否是商品卡片或其内部元素
    const productCard = e.target.closest('.product-card');
    
    // 如果是商品卡片，并且点击的不是按钮
    if (productCard && !e.target.closest('button')) {
        // 获取商品ID
        const productId = productCard.dataset.productId || '1';
        
        // 跳转到商品详情页
        window.location.href = '/product/' + productId;
    }
}); 