document.addEventListener('DOMContentLoaded', () => {
    const roleRadios = document.querySelectorAll('input[name="role"]');
    const responsableIdGroup = document.getElementById('responsableIdGroup');
    const responsableIdInput = document.getElementById('responsableId');

    function toggleResponsableIdField() {
        if (document.getElementById('roleTechnician').checked) {
            responsableIdGroup.style.display = 'block';
            responsableIdInput.setAttribute('required', 'true');
        } else {
            responsableIdGroup.style.display = 'none';
            responsableIdInput.removeAttribute('required');
        }
    }

    // Initial check on page load
    toggleResponsableIdField();

    // Add event listeners to role radio buttons
    roleRadios.forEach(radio => {
        radio.addEventListener('change', toggleResponsableIdField);
    });

    const registerForm = document.querySelector('form'); // Get the form element

    registerForm.addEventListener('submit', async (event) => {
        event.preventDefault(); // Prevent default form submission

        const firstName = document.getElementById('firstName').value;
        const lastName = document.getElementById('lastName').value;
        const email = document.getElementById('email').value;
        const password = document.getElementById('password').value;
        const confirmPassword = document.getElementById('confirmPassword').value;
        const phoneNumber = document.getElementById('phoneNumber').value;
        const role = document.querySelector('input[name="role"]:checked').value;
        const responsableId = document.getElementById('responsableId').value;

        if (password !== confirmPassword) {
            alert('Passwords do not match!');
            return;
        }

        // Client-side password validation
        const passwordPattern = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;
        if (!passwordPattern.test(password)) {
            alert('Password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one digit, and one special character (@$!%*?&).');
            return;
        }

        const registerData = {
            firstName: firstName,
            lastName: lastName,
            email: email,
            password: password,
            phoneNumber: phoneNumber,
            role: role
        };

        // Add responsableId only if the role is TECHNICIAN and it's provided
        if (role === 'TECHNICIAN' && responsableId) {
            registerData.responsableId = parseInt(responsableId);
        } else if (role === 'TECHNICIAN' && !responsableId) {
            alert('Responsable ID is required for a Technician!');
            return;
        }

        try {
            const response = await fetch('/api/auth/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(registerData)
            });

            if (response.ok) {
                alert('Registration successful!');
                window.location.href = '/'; // Redirect to login page or home
            } else {
                const errorData = await response.text();
                alert(`Registration failed: ${errorData}`);
            }
        } catch (error) {
            console.error('Error during registration:', error);
            alert('An error occurred during registration.');
        }
    });
}); 