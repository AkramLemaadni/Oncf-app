// Function to handle assessment form submission
async function submitAssessment(event) {
    event.preventDefault();
    
    // Get form data
    const form = event.target;
    const technicianId = form.querySelector('input[name="technicianId"]').value;
    const score = form.querySelector('input[name="score"]:checked').value;
    const comments = form.querySelector('#comments').value;
    
    try {
        // Get JWT token
        const token = localStorage.getItem('jwtToken');
        if (!token) {
            alert('Authentication token not found. Please log in again.');
            window.location.href = '/login';
            return;
        }
        
        // Submit assessment
        const response = await fetch('/engineer/submit-assessment', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({
                technicianId: technicianId,
                score: parseInt(score),
                comments: comments
            })
        });
        
        if (response.ok) {
            // Show success message
            alert('Assessment submitted successfully!');
            
            // Use the navigateTo function from auth.js for secure redirection
            navigateTo('/engineer/technicians');
        } else {
            const errorData = await response.text();
            throw new Error(errorData || 'Failed to submit assessment');
        }
    } catch (error) {
        console.error('Error submitting assessment:', error);
        alert('Failed to submit assessment: ' + error.message);
    }
}

// Initialize form when document is loaded
document.addEventListener('DOMContentLoaded', function() {
    // Set default score to 5
    document.getElementById('score-5').checked = true;
    
    // Add submit handler to the form
    const form = document.getElementById('assessmentForm');
    if (form) {
        form.addEventListener('submit', submitAssessment);
    }
}); 