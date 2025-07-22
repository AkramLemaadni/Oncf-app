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
        
        return originalFetch.apply(window, args);
    };
    
    // Add Authorization header with JWT token to all XMLHttpRequest
    const originalOpen = XMLHttpRequest.prototype.open;
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
        
        return originalOpen.apply(xhr, args);
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
    }
});

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
            
            // Clear token regardless of response
            localStorage.removeItem('jwtToken');
            console.log("JWT token removed from localStorage");
        } else {
            console.log("No token found in localStorage");
        }
    } catch (error) {
        console.error("Error during logout:", error);
    } finally {
        // Always redirect to login page
        console.log("Redirecting to login page...");
        window.location.href = '/login';
    }
}; 