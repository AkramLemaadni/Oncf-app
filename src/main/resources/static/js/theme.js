document.addEventListener('DOMContentLoaded', () => {
    const themeToggle = document.getElementById('theme-toggle');
    const body = document.body;
    const darkIcon = document.querySelector('.dark-icon');
    const lightIcon = document.querySelector('.light-icon');

    // Load saved theme from localStorage
    const savedTheme = localStorage.getItem('theme');
    if (savedTheme) {
        // Handle both formats of theme storage
        if (savedTheme === 'light' || savedTheme === 'light-mode') {
            body.classList.add('light-mode');
            if (darkIcon && lightIcon) {
                darkIcon.style.opacity = '1';
                lightIcon.style.opacity = '0';
            }
        } else {
            body.classList.add('dark-mode');
            if (darkIcon && lightIcon) {
                darkIcon.style.opacity = '0';
                lightIcon.style.opacity = '1';
            }
        }
    }

    themeToggle.addEventListener('click', () => {
        if (body.classList.contains('light-mode')) {
            body.classList.remove('light-mode');
            body.classList.add('dark-mode');
            localStorage.setItem('theme', 'dark');
            if (darkIcon && lightIcon) {
                darkIcon.style.opacity = '0';
                lightIcon.style.opacity = '1';
            }
        } else {
            body.classList.remove('dark-mode');
            body.classList.add('light-mode');
            localStorage.setItem('theme', 'light');
            if (darkIcon && lightIcon) {
                darkIcon.style.opacity = '1';
                lightIcon.style.opacity = '0';
            }
        }
    });
}); 