document.addEventListener('DOMContentLoaded', () => {
    // Tab switching logic
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
    });

    signupTabBtn.addEventListener('click', () => {
        isLoginMode = false;
        signupTabBtn.classList.add('active');
        loginTabBtn.classList.remove('active');
        nameFieldContainer.classList.remove('hidden');
        submitBtn.textContent = 'Create account';
    });

    authForm.addEventListener('submit', (e) => {
        e.preventDefault();
        const email = document.getElementById('email').value;
        const password = document.getElementById('password').value;

        if (isLoginMode) {
            console.log('Logging in with:', email);
            // Insert login handler call here
        } else {
            const name = document.getElementById('name').value;
            console.log('Creating account for:', name, email);
            // Insert signup handler call here
        }
    });

    // Wireframe Topography Canvas Animation Background
    const canvas = document.getElementById('bgCanvas');
    const ctx = canvas.getContext('2d');

    let width, height;
    let particles = [];
    const spacing = 35;
    let time = 0;

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

                // Create wave heights resembling mountain/topography wireframes
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