document.addEventListener('DOMContentLoaded', () => {
    const loginTabBtn = document.getElementById('loginTabBtn');
    const signupTabBtn = document.getElementById('signupTabBtn');
    const nameFieldContainer = document.getElementById('nameFieldContainer');
    const submitBtn = document.getElementById('submitBtn');
    const authForm = document.getElementById('authForm');

    let isLoginMode = true;

    loginTabBtn.addEventListener('click', () => {
        isLoginMode = true;
        loginTabBtn.classList.add('active');
        signupTabBtn.classList.remove('active');
        nameFieldContainer.classList.add('hidden');
        submitBtn.textContent = 'Log in';
        document.getElementById('name').removeAttribute('required');
    });

    signupTabBtn.addEventListener('click', () => {
        isLoginMode = false;
        signupTabBtn.classList.add('active');
        loginTabBtn.classList.remove('active');
        nameFieldContainer.classList.remove('hidden');
        submitBtn.textContent = 'Create account';
        document.getElementById('name').setAttribute('required', 'true');
    });

    authForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const email = document.getElementById('email').value.trim();
        const password = document.getElementById('password').value.trim();
        const name = document.getElementById('name').value.trim();

        submitBtn.disabled = true;
        submitBtn.textContent = isLoginMode ? 'Logging in...' : 'Creating account...';

        try {
            const API_BASE_URL = window.API_BASE_URL || 'https://ledgerly-2-urdh.onrender.com';
            const endpoint = isLoginMode ? '/api/auth/login' : '/api/auth/register';
            
            const payload = isLoginMode 
                ? { email, password } 
                : { name, email, password };

            const response = await fetch(`${API_BASE_URL}${endpoint}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(payload)
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || 'Authentication failed. Please check your credentials.');
            }

            const data = await response.json();
            
            if (data.token) {
                localStorage.setItem('token', data.token);
            }
            if (data.user) {
                localStorage.setItem('user', JSON.stringify(data.user));
            }

            window.location.href = 'dashboard.html';

        } catch (error) {
            alert(error.message || 'An error occurred. Please ensure your backend service is running.');
            submitBtn.disabled = false;
            submitBtn.textContent = isLoginMode ? 'Log in' : 'Create account';
        }
    });

    const canvas = document.getElementById('bgCanvas');
    const ctx = canvas.getContext('2d');

    let width, height;
    let time = 0;
    const spacing = 35;

    function resize() {
        width = canvas.width = window.innerWidth;
        height = canvas.height = window.innerHeight;
    }

    window.addEventListener('resize', resize);
    resize();

    function drawTopography() {
        ctx.clearRect(0, 0, width, height);
        time += 0.008;

        ctx.strokeStyle = 'rgba(255, 255, 255, 0.08)';
        ctx.lineWidth = 1;

        const cols = Math.floor(width / spacing) + 2;
        const rows = Math.floor(height / spacing) + 2;

        for (let y = 0; y < rows; y++) {
            ctx.beginPath();
            for (let x = 0; x < cols; x++) {
                const px = (x - 1) * spacing;
                const py = (y - 1) * spacing;

                const wave1 = Math.sin(x * 0.3 + time * 1.5) * Math.cos(y * 0.3 + time) * 25;
                const wave2 = Math.sin((x * y) * 0.01 + time) * 15;
                const elevation = wave1 + wave2;

                const finalY = py + elevation;

                if (x === 0) {
                    ctx.moveTo(px, finalY);
                } else {
                    ctx.lineTo(px, finalY);
                }
            }
            ctx.stroke();
        }

        requestAnimationFrame(drawTopography);
    }

    drawTopography();
});