// Function to switch language while preserving authentication
function switchLanguage(lang) {
    // Get current URL
    const currentUrl = window.location.pathname;
    const token = localStorage.getItem('jwtToken');
    
    // Create form for POST request
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = currentUrl + '?lang=' + lang;
    
    // Add CSRF token if needed
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
    if (csrfToken && csrfHeader) {
        const csrfInput = document.createElement('input');
        csrfInput.type = 'hidden';
        csrfInput.name = csrfHeader;
        csrfInput.value = csrfToken;
        form.appendChild(csrfInput);
    }
    
    // Add JWT token
    if (token) {
        const tokenInput = document.createElement('input');
        tokenInput.type = 'hidden';
        tokenInput.name = 'Authorization';
        tokenInput.value = 'Bearer ' + token;
        form.appendChild(tokenInput);
    }
    
    // Submit form
    document.body.appendChild(form);
    form.submit();
} 