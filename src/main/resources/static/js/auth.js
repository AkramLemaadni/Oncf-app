// This script ensures that the JWT token is sent with every request
document.addEventListener('DOMContentLoaded', () => {
    console.log("Auth.js loaded");
    
    // Check if token exists in localStorage
    const token = localStorage.getItem('jwtToken');
    console.log("Token exists in localStorage:", !!token);
    
    if (token) {
        console.log("Token found:", token.substring(0, 10) + "...");
    }
    
    // Add Authorization header with JWT token to all fetch requests
    const originalFetch = window.fetch;
    window.fetch = function() {
        let args = Array.prototype.slice.call(arguments);
        const token = localStorage.getItem('jwtToken');
        
        if (token) {
            if (!args[1]) {
                args[1] = {};
            }
            if (!args[1].headers) {
                args[1].headers = {};
            }
            
            // Convert headers to plain object if it's a Headers instance
            if (args[1].headers instanceof Headers) {
                const headersObj = {};
                for (const [key, value] of args[1].headers.entries()) {
                    headersObj[key] = value;
                }
                args[1].headers = headersObj;
            }
            
            args[1].headers['Authorization'] = `Bearer ${token}`;
            console.log(`Adding Authorization header to ${args[0]}`);
        } else {
            console.log(`No token available for request to ${args[0]}`);
        }
        
        return originalFetch.apply(window, args).then(response => {
            if (response.status === 401) {
                // Token might be expired or invalid
                console.error("Authentication failed - redirecting to login");
                localStorage.removeItem('jwtToken');
                window.location.href = '/login';
                return Promise.reject('Authentication failed');
            }
            return response;
        });
    };
    
    // Add Authorization header with JWT token to all XMLHttpRequest
    const originalXHROpen = XMLHttpRequest.prototype.open;
    XMLHttpRequest.prototype.open = function() {
        const token = localStorage.getItem('jwtToken');
        const args = Array.prototype.slice.call(arguments);
        
        const method = args[0];
        const url = args[1];
        
        const xhr = this;
        const originalSend = xhr.send;
        
        xhr.send = function() {
            if (token) {
                xhr.setRequestHeader('Authorization', `Bearer ${token}`);
                console.log(`Adding Authorization header to XHR request to ${url}`);
            } else {
                console.log(`No token available for XHR request to ${url}`);
            }
            return originalSend.apply(xhr, arguments);
        };
        
        // Handle 401 responses
        xhr.addEventListener('load', function() {
            if (xhr.status === 401) {
                console.error("Authentication failed - redirecting to login");
                localStorage.removeItem('jwtToken');
                window.location.href = '/login';
            }
        });
        
        return originalXHROpen.apply(xhr, args);
    };
    
    // Check if user is logged in
    const isLoggedIn = !!localStorage.getItem('jwtToken');
    console.log("User is logged in:", isLoggedIn);
    
    // If on a protected page and not logged in, redirect to login
    const isProtectedPage = window.location.pathname.startsWith('/engineer/') || 
                           window.location.pathname.startsWith('/technician/');
    
    if (isProtectedPage && !isLoggedIn) {
        console.log("Protected page detected but user is not logged in. Redirecting to login page.");
        window.location.href = '/login';
        return;
    }

    // If logged in and on a protected page, verify the token
    if (isLoggedIn && isProtectedPage) {
        verifyToken();
    }
});

// Function to verify token validity
async function verifyToken() {
    const token = localStorage.getItem('jwtToken');
    if (!token) return;

    try {
        const response = await fetch('/api/auth/verify', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (!response.ok) {
            console.error("Token verification failed");
            localStorage.removeItem('jwtToken');
            window.location.href = '/login';
        }
    } catch (error) {
        console.error("Error verifying token:", error);
    }
}

// Function to switch language while preserving authentication
window.switchLanguageAuthenticated = function(lang) {
    const token = localStorage.getItem('jwtToken');
    if (!token) {
        console.error("No JWT token found, redirecting to login");
        window.location.href = '/login';
        return;
    }

    // Get current URL and add language parameter
    let url = new URL(window.location.href);
    url.searchParams.set('lang', lang);
    
    // Make authenticated request to change language
    fetch(url.toString(), {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Accept': 'text/html'
        }
    }).then(response => {
        if (response.ok) {
            return response.text();
        } else if (response.status === 401) {
            // Token expired or invalid
            localStorage.removeItem('jwtToken');
            window.location.href = '/login';
            throw new Error('Authentication failed');
        } else {
            throw new Error('Language switch failed');
        }
    }).then(html => {
        // Replace current page content with new content
        document.open();
        document.write(html);
        document.close();
        
        // Update URL without page reload
        window.history.pushState({}, '', url.toString());
    }).catch(error => {
        console.error('Error switching language:', error);
        // Fallback: reload page with new language parameter
        window.location.href = url.toString();
    });
};

// Global logout function
window.handleLogout = async function() {
    try {
        console.log("Initiating logout process...");
        const token = localStorage.getItem('jwtToken');
        
        if (token) {
            const response = await fetch('/api/auth/logout', {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });

            console.log("Logout response status:", response.status);
        }
    } catch (error) {
        console.error("Error during logout:", error);
    } finally {
        // Clear token and redirect to login
        localStorage.removeItem('jwtToken');
        window.location.href = '/login';
    }
};

// Function to handle authenticated navigation
window.navigateTo = function(url) {
    const token = localStorage.getItem('jwtToken');
    if (!token) {
        console.error("No JWT token found, redirecting to login");
        window.location.href = '/login';
        return;
    }

    // Create a temporary form to submit the request with the token
    const form = document.createElement('form');
    form.method = 'GET';
    form.action = url;
    form.style.display = 'none';

    // Add the token as a hidden field
    const tokenField = document.createElement('input');
    tokenField.type = 'hidden';
    tokenField.name = 'Authorization';
    tokenField.value = `Bearer ${token}`;
    form.appendChild(tokenField);

    // Submit the form
    document.body.appendChild(form);
    form.submit();
}; 