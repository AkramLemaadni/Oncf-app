document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');

    if (loginForm) {
        loginForm.addEventListener('submit', async (event) => {
            event.preventDefault(); // Prevent default form submission

            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;

            try {
                // Explicitly use the correct endpoint URL
                const response = await fetch('/api/auth/login', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ email, password })
                });

                if (response.ok) {
                    const data = await response.json();
                    console.log("Login successful, received token:", data.token);
                    
                    // Store the JWT token in localStorage
                    localStorage.setItem('jwtToken', data.token);
                    
                    // Make a request to engineer/home with the token
                    const homeResponse = await fetch('/engineer/home', {
                        headers: {
                            'Authorization': `Bearer ${data.token}`
                        }
                    });

                    if (homeResponse.ok) {
                        const htmlContent = await homeResponse.text();
                        document.open();
                        document.write(htmlContent);
                        document.close();
                    } else {
                        console.error('Failed to load home page:', homeResponse.status);
                        alert('Failed to load home page. Please try again.');
                    }
                } else {
                    const errorData = await response.text();
                    alert('Login failed: ' + errorData);
                }
            } catch (error) {
                console.error('Error during login:', error);
                alert('An error occurred. Please try again later.');
            }
        });
    }
}); 