// Statistics page functionality
let currentSessionId = null;
// Global variable to store current chart
let currentChart = null;
// Global variable to store translations
let translations = {};
// Flag to prevent multiple simultaneous dataset loading
let datasetsLoading = false;
// Flag to prevent multiple initializations
let pageInitialized = false;

// Initialize page
document.addEventListener('DOMContentLoaded', function() {
    if (window.location.pathname.includes('/stats')) {
        initializeStatsPage();
    }
});

// Also check when the page is fully loaded (in case content changes after DOM ready)
window.addEventListener('load', function() {
    if (window.location.pathname.includes('/stats')) {
        // Add a delay to ensure the page is fully settled after language change
        setTimeout(() => {
            // Reload translations in case page content changed
            loadTranslations();
            
            // Ensure datasets are loaded even after language switches
            ensureDatasetsLoaded();
            
            // If there's already a chart displayed, refresh it with new language
            refreshChartWithNewLanguage();
        }, 500); // Small delay to ensure everything is ready
    }
});

// Add an additional check specifically for language changes
document.addEventListener('DOMContentLoaded', function() {
    // Check if this is a language change by looking for URL parameters
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('lang') && window.location.pathname.includes('/stats')) {
        console.log('Language change detected, reinitializing...');
        // Reset initialization flag for language changes
        pageInitialized = false;
        datasetsLoading = false;
        
        // This is a language change, ensure everything loads properly
        setTimeout(() => {
            initializeStatsPage();
        }, 1000); // Longer delay for language changes
    }
});

// Function to initialize the stats page
function initializeStatsPage() {
    if (pageInitialized) {
        console.log('Page already initialized, skipping...');
        return;
    }
    
    try {
        console.log('Initializing stats page...');
        pageInitialized = true;
        
        // Load translations first
        loadTranslations();
        
        // Setup file upload
        setupFileUpload();
        
        // Load datasets with retry mechanism
        loadDatasetsWithRetry();
        
        // Set up a watchdog to check if datasets get stuck in loading state
        setTimeout(() => {
            checkForStuckLoading();
        }, 3000); // Check after 3 seconds
        
        console.log('Stats page initialized successfully');
    } catch (error) {
        console.error('Error initializing stats page:', error);
        pageInitialized = false; // Reset flag on error
        // Retry after a short delay
        setTimeout(initializeStatsPage, 2000);
    }
}

// Function to check if datasets are stuck in loading state
function checkForStuckLoading() {
    const container = document.getElementById('datasetsContainer');
    if (!container) return;
    
    // If still showing loading text after 3 seconds, force reload
    if (container.innerHTML.includes('Loading datasets...') || 
        container.innerHTML.includes('Chargement des jeux de données...')) {
        console.log('Datasets stuck in loading state, forcing reload...');
        // Reset the loading flag and try again
        datasetsLoading = false;
        loadDatasetsWithRetry();
    } else {
        console.log('Datasets loading completed successfully, no action needed');
    }
}

// Function to ensure datasets are loaded (with retry mechanism)
function ensureDatasetsLoaded() {
    if (datasetsLoading) {
        console.log('Datasets currently loading, waiting...');
        return;
    }
    
    const container = document.getElementById('datasetsContainer');
    if (!container) {
        console.log('Datasets container not found');
        return;
    }
    
    console.log('Checking dataset loading status:', container.innerHTML.substring(0, 100) + '...');
    
    // Check if datasets are already loaded properly
    const hasDatasets = container.innerHTML && 
                       !container.innerHTML.includes('Loading datasets...') && 
                       !container.innerHTML.includes('Chargement des jeux de données...') &&
                       (container.innerHTML.includes('dataset-card') || container.innerHTML.includes('empty-state'));
    
    if (!hasDatasets) {
        console.log('Datasets not loaded properly, forcing reload...');
        // Force reload with a small delay, but only if not already loading
        setTimeout(() => {
            if (!datasetsLoading) {
                loadDatasetsWithRetry();
            }
        }, 200);
    } else {
        console.log('Datasets already loaded successfully');
    }
}

// Function to load datasets with retry mechanism
function loadDatasetsWithRetry(retryCount = 0) {
    const maxRetries = 3;
    
    loadDatasets().catch(error => {
        console.error('Error loading datasets:', error);
        
        if (retryCount < maxRetries) {
            console.log(`Retrying dataset load (${retryCount + 1}/${maxRetries})`);
            setTimeout(() => {
                loadDatasetsWithRetry(retryCount + 1);
            }, 1000 * (retryCount + 1)); // Exponential backoff
        } else {
            console.error('Failed to load datasets after', maxRetries, 'retries');
            const container = document.getElementById('datasetsContainer');
            if (container) {
                container.innerHTML = `
                    <div class="empty-state">
                        <i class="fas fa-exclamation-triangle"></i>
                        <h3>Error loading datasets</h3>
                        <p>Please refresh the page or try again</p>
                        <button class="btn-primary" onclick="loadDatasetsWithRetry()">
                            <i class="fas fa-sync-alt"></i> Retry
                        </button>
                    </div>
                `;
            }
        }
    });
}

// Function to refresh chart with new language
function refreshChartWithNewLanguage() {
    if (currentChart && document.getElementById('incidentSection').style.display !== 'none') {
        const chartData = currentChart.data;
        if (chartData && chartData.labels) {
            // Recreate the chart with new translations
            const incidentData = {
                trainNames: chartData.labels,
                incidentCounts: chartData.datasets[0].data,
                totalIncidents: chartData.datasets[0].data.reduce((a, b) => a + b, 0),
                totalTrains: chartData.labels.length
            };
            
            // Update summary if visible
            const summaryContainer = document.getElementById('incidentSummary');
            if (summaryContainer && summaryContainer.innerHTML.includes('summary-card')) {
                summaryContainer.innerHTML = `
                    <div class="incident-summary">
                        <div class="summary-card">
                            <div class="summary-number">${incidentData.totalIncidents}</div>
                            <div class="summary-label">${getText('engineer.stats.incidents.summary.total')}</div>
                        </div>
                        <div class="summary-card">
                            <div class="summary-number">${incidentData.totalTrains}</div>
                            <div class="summary-label">${getText('engineer.stats.incidents.summary.trains')}</div>
                        </div>
                        <div class="summary-card">
                            <div class="summary-number">${incidentData.incidentCounts[0] || 0}</div>
                            <div class="summary-label">${getText('engineer.stats.incidents.summary.highest')}</div>
                        </div>
                    </div>
                `;
            }
            
            // Recreate chart
            createIncidentChart(incidentData);
        }
    }
}

// Function to get current language
function getCurrentLanguage() {
    // Check URL parameter first
    const urlParams = new URLSearchParams(window.location.search);
    const langParam = urlParams.get('lang');
    if (langParam) {
        return langParam;
    }
    
    // Check for language switcher state
    const langSwitchers = document.querySelectorAll('[onclick*="switchLanguage"]');
    for (let switcher of langSwitchers) {
        if (switcher.textContent.trim() === 'EN') {
            // If the button shows 'EN', it means we're currently in French
            return 'fr';
        }
    }
    
    // Check for French indicators in the page
    const pageTitle = document.title || '';
    const pageContent = document.body.textContent || '';
    
    // Look for French text indicators
    if (pageTitle.includes('Statistiques') || 
        pageContent.includes('Statistiques') ||
        pageContent.includes('Tableau de Bord') ||
        pageContent.includes('Analyse des Incidents') ||
        pageContent.includes('Jeux de Données') ||
        pageContent.includes('Vos Jeux de Données') ||
        pageContent.includes('Actualiser') ||
        document.querySelector('.page-title')?.textContent?.includes('Statistiques')) {
        return 'fr';
    }
    
    return 'en';
}

// Function to load translations
function loadTranslations() {
    const currentLang = getCurrentLanguage();
    console.log('Detected language:', currentLang); // Debug log
    
    // Use direct fallback translations - no API call
    if (currentLang === 'fr') {
        translations = {
            'engineer.stats.incidents.chart.title': 'Analyse des Incidents de Train - {0} Trains au Total',
            'engineer.stats.incidents.chart.dataset_label': 'Nombre d\'Incidents',
            'engineer.stats.incidents.chart.x_axis': 'Identifiant du Train',
            'engineer.stats.incidents.chart.y_axis': 'Nombre d\'Incidents',
            'engineer.stats.incidents.chart.tooltip': 'Train {0}: {1} incidents',
            'engineer.stats.incidents.summary.total': 'Total des Incidents',
            'engineer.stats.incidents.summary.trains': 'Trains Impliqués',
            'engineer.stats.incidents.summary.highest': 'Compte le Plus Élevé',
            'engineer.stats.incidents.analysis_completed': 'Analyse des incidents terminée !'
        };
    } else {
        translations = {
            'engineer.stats.incidents.chart.title': 'Train Incident Analysis - {0} Trains Total',
            'engineer.stats.incidents.chart.dataset_label': 'Number of Incidents',
            'engineer.stats.incidents.chart.x_axis': 'Train Identifier',
            'engineer.stats.incidents.chart.y_axis': 'Number of Incidents',
            'engineer.stats.incidents.chart.tooltip': 'Train {0}: {1} incidents',
            'engineer.stats.incidents.summary.total': 'Total Incidents',
            'engineer.stats.incidents.summary.trains': 'Trains Involved',
            'engineer.stats.incidents.summary.highest': 'Highest Count',
            'engineer.stats.incidents.analysis_completed': 'Incident analysis completed!'
        };
    }
    
    console.log('Loaded translations:', translations); // Debug log
}

// Function to get translated text
function getText(key, ...params) {
    let text = translations[key] || key;
    // Replace placeholders {0}, {1}, etc. with parameters
    params.forEach((param, index) => {
        text = text.replace(`{${index}}`, param);
    });
    return text;
}

function setupFileUpload() {
    const uploadArea = document.getElementById('uploadArea');
    const fileInput = document.getElementById('fileInput');
    
    if (!uploadArea || !fileInput) return;
    
    // Drag and drop functionality
    uploadArea.addEventListener('dragover', (e) => {
        e.preventDefault();
        uploadArea.classList.add('dragover');
    });
    
    uploadArea.addEventListener('dragleave', () => {
        uploadArea.classList.remove('dragover');
    });
    
    uploadArea.addEventListener('drop', (e) => {
        e.preventDefault();
        uploadArea.classList.remove('dragover');
        
        const files = e.dataTransfer.files;
        if (files.length > 0) {
            handleFileUpload(files[0]);
        }
    });
    
    // File input change
    fileInput.addEventListener('change', (e) => {
        if (e.target.files.length > 0) {
            handleFileUpload(e.target.files[0]);
        }
    });
}

async function handleFileUpload(file) {
    // Validate file type
    if (!file.name.toLowerCase().endsWith('.xlsx')) {
        alert('Please select a .xlsx file');
        return;
    }
    
    // Check if user is authenticated
    const token = localStorage.getItem('jwtToken');
    if (!token) {
        showNotification('Please log in to upload files', 'error');
        window.location.href = '/login';
        return;
    }
    
    // Show progress
    const progressBar = document.getElementById('uploadProgress');
    const progressFill = progressBar.querySelector('.progress-fill');
    progressBar.style.display = 'block';
    progressFill.style.width = '10%';
    
    try {
        const formData = new FormData();
        formData.append('file', file);
        
        progressFill.style.width = '50%';
        
        const response = await fetch('/engineer/api/upload-xlsx', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Accept': 'application/json'
            },
            body: formData
        });
        
        progressFill.style.width = '90%';
        
        // Check if response is ok before trying to parse JSON
        if (!response.ok) {
            // Try to get error message from response
            let errorMessage = 'Upload failed';
            const contentType = response.headers.get('content-type');
            
            if (contentType && contentType.includes('application/json')) {
                const errorData = await response.json();
                errorMessage = errorData.message || errorData.error || 'Upload failed';
            } else {
                // Server returned HTML (likely error page)
                errorMessage = `Server error (${response.status}). Please check your authentication.`;
            }
            
            throw new Error(errorMessage);
        }
        
        const result = await response.json();
        
        progressFill.style.width = '100%';
        
        if (result.success) {
            showNotification('File uploaded successfully!', 'success');
            loadDatasets(); // Refresh datasets
            
            // Clear file input
            document.getElementById('fileInput').value = '';
        } else {
            showNotification('Upload failed: ' + (result.message || 'Unknown error'), 'error');
        }
    } catch (error) {
        console.error('Upload error:', error);
        
        // Handle specific authentication errors
        if (error.message.includes('401') || error.message.includes('authentication')) {
            localStorage.removeItem('jwtToken');
            showNotification('Session expired. Please log in again.', 'error');
            setTimeout(() => {
                window.location.href = '/login';
            }, 2000);
        } else {
            showNotification('Upload failed: ' + error.message, 'error');
        }
    } finally {
        // Hide progress bar after 1 second
        setTimeout(() => {
            progressBar.style.display = 'none';
            progressFill.style.width = '0%';
        }, 1000);
    }
}

async function loadDatasets() {
    if (datasetsLoading) {
        console.log('Datasets already loading, skipping duplicate request...');
        return;
    }
    
    const container = document.getElementById('datasetsContainer');
    if (!container) {
        throw new Error('Datasets container not found');
    }
    
    datasetsLoading = true;
    console.log('Starting dataset loading...');
    
    // Show loading state with current language
    const currentLang = getCurrentLanguage();
    console.log('Current language for loading:', currentLang);
    const loadingText = currentLang === 'fr' ? 'Chargement des jeux de données...' : 'Loading datasets...';
    container.innerHTML = `<div class="loading">${loadingText}</div>`;
    
    try {
        // Check authentication first
        const token = localStorage.getItem('jwtToken');
        if (!token) {
            throw new Error('No authentication token found');
        }
        
        const response = await fetch('/engineer/api/files', {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Accept': 'application/json'
            }
        });
        
        if (!response.ok) {
            if (response.status === 401) {
                localStorage.removeItem('jwtToken');
                window.location.href = '/login';
                return;
            }
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        
        const files = await response.json();
        
        if (!Array.isArray(files)) {
            throw new Error('Invalid response format');
        }
        
        if (files.length === 0) {
            const emptyText = currentLang === 'fr' 
                ? 'Aucun jeu de données téléchargé pour le moment'
                : 'No datasets uploaded yet';
            const emptySubtext = currentLang === 'fr'
                ? 'Téléchargez votre premier fichier Excel pour commencer l\'analyse de données'
                : 'Upload your first Excel file to get started with data analysis';
                
            container.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-folder-open"></i>
                    <h3>${emptyText}</h3>
                    <p>${emptySubtext}</p>
                </div>
            `;
            return;
        }
        
        const grid = document.createElement('div');
        grid.className = 'datasets-grid';
        
        files.forEach(file => {
            const card = document.createElement('div');
            card.className = 'dataset-card';
            
            // Get button text based on language
            const viewText = currentLang === 'fr' ? 'Voir les Données' : 'View Data';
            const incidentsText = currentLang === 'fr' ? 'Voir les Incidents' : 'View Incidents';
            const deleteText = currentLang === 'fr' ? 'Supprimer' : 'Delete';
            
            card.innerHTML = `
                <div class="dataset-title">${file.fileName}</div>
                <div class="dataset-info">
                    <div><i class="fas fa-calendar"></i> ${new Date(file.uploadDate).toLocaleString()}</div>
                    <div><i class="fas fa-list"></i> ${file.rowCount} rows</div>
                    <div><i class="fas fa-columns"></i> ${file.columnCount} columns</div>
                    <div><i class="fas fa-file"></i> ${formatFileSize(file.fileSize)}</div>
                </div>
                <div style="margin-top: 15px;">
                    <button class="btn-primary" onclick="viewData('${file.fileId}')">
                        <i class="fas fa-eye"></i> ${viewText}
                    </button>
                    <button class="btn-secondary" onclick="showIncidents('${file.fileId}')">
                        <i class="fas fa-exclamation-triangle"></i> ${incidentsText}
                    </button>
                    <button class="btn-danger" onclick="deleteDataset('${file.fileId}')">
                        <i class="fas fa-trash"></i> ${deleteText}
                    </button>
                </div>
            `;
            grid.appendChild(card);
        });
        
        container.innerHTML = '';
        container.appendChild(grid);
        
        console.log('Datasets loaded successfully:', files.length, 'files');
        console.log('UI updated with dataset grid');
        
    } catch (error) {
        console.error('Error loading datasets:', error);
        
        const errorText = currentLang === 'fr' 
            ? 'Erreur lors du chargement des jeux de données'
            : 'Error loading datasets';
        const retryText = currentLang === 'fr' ? 'Réessayer' : 'Retry';
        
        container.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-exclamation-triangle"></i>
                <h3>${errorText}</h3>
                <p>${error.message}</p>
                <button class="btn-primary" onclick="loadDatasetsWithRetry()">
                    <i class="fas fa-sync-alt"></i> ${retryText}
                </button>
            </div>
        `;
        
        throw error; // Re-throw for retry mechanism
    } finally {
        datasetsLoading = false; // Reset flag when done
        console.log('Dataset loading process completed');
    }
}

function formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

async function viewData(fileId) {
    const section = document.getElementById('dataViewSection');
    const container = document.getElementById('dataViewContainer');
    
    if (!section || !container) return;
    
    section.style.display = 'block';
    container.innerHTML = '<div class="loading">Loading data...</div>';
    
    try {
        const response = await fetch(`/engineer/api/file/${fileId}`);
        const fileData = await response.json();
        
        if (!fileData) {
            container.innerHTML = '<div class="empty-state">No data found</div>';
            return;
        }
        
        // Create data table
        const table = document.createElement('table');
        table.className = 'data-table';
        
        // Create header
        const thead = document.createElement('thead');
        const headerRow = document.createElement('tr');
        fileData.headers.forEach(header => {
            const th = document.createElement('th');
            th.textContent = header;
            headerRow.appendChild(th);
        });
        thead.appendChild(headerRow);
        table.appendChild(thead);
        
        // Create body
        const tbody = document.createElement('tbody');
        const maxRows = Math.min(100, fileData.data.length); // Limit to first 100 rows
        
        for (let i = 0; i < maxRows; i++) {
            const row = fileData.data[i];
            const tr = document.createElement('tr');
            fileData.headers.forEach(header => {
                const td = document.createElement('td');
                td.textContent = row[header] || '';
                tr.appendChild(td);
            });
            tbody.appendChild(tr);
        }
        table.appendChild(tbody);
        
        container.innerHTML = `
            <div style="margin-bottom: 15px; color: #bdc3c7;">
                <strong>File:</strong> ${fileData.fileName} | 
                <strong>Total Rows:</strong> ${fileData.rowCount} | 
                <strong>File Size:</strong> ${formatFileSize(fileData.fileSize)} |
                <strong>Showing:</strong> First ${maxRows} rows
            </div>
        `;
        container.appendChild(table);
        
        // Scroll to data view
        section.scrollIntoView({ behavior: 'smooth' });
        
    } catch (error) {
        console.error('Error loading data:', error);
        container.innerHTML = '<div class="empty-state">Error loading data</div>';
    }
}

async function showIncidents(fileId) {
    const section = document.getElementById('incidentSection');
    const summaryContainer = document.getElementById('incidentSummary');
    
    if (!section || !summaryContainer) return;
    
    section.style.display = 'block';
    summaryContainer.innerHTML = '<div class="loading">Analyzing incidents...</div>';
    
    try {
        const response = await fetch(`/engineer/api/file/${fileId}/incidents`);
        const incidentData = await response.json();
        
        if (!incidentData || incidentData.trainNames.length === 0) {
            summaryContainer.innerHTML = '<div class="empty-state">No train incidents found in this dataset</div>';
            return;
        }
        
        // Display summary
        summaryContainer.innerHTML = `
            <div class="incident-summary">
                <div class="summary-card">
                    <div class="summary-number">${incidentData.totalIncidents}</div>
                    <div class="summary-label">${getText('engineer.stats.incidents.summary.total')}</div>
                </div>
                <div class="summary-card">
                    <div class="summary-number">${incidentData.totalTrains}</div>
                    <div class="summary-label">${getText('engineer.stats.incidents.summary.trains')}</div>
                </div>
                <div class="summary-card">
                    <div class="summary-number">${incidentData.incidentCounts[0] || 0}</div>
                    <div class="summary-label">${getText('engineer.stats.incidents.summary.highest')}</div>
                </div>
            </div>
        `;
        
        // Create incident chart
        createIncidentChart(incidentData);
        
        // Scroll to incident section
        section.scrollIntoView({ behavior: 'smooth' });
        
    } catch (error) {
        console.error('Error loading incident data:', error);
        summaryContainer.innerHTML = '<div class="empty-state">Error loading incident data</div>';
    }
}

function createIncidentChart(incidentData) {
    // Destroy existing chart
    if (currentChart) {
        currentChart.destroy();
    }
    
    const ctx = document.getElementById('incidentChart')?.getContext('2d');
    if (!ctx) return;
    
    // Show all trains - no artificial limit
    const trainNames = incidentData.trainNames;
    const incidentCounts = incidentData.incidentCounts;
    
    // Generate colors for bars
    const colors = generateChartColors(trainNames.length);
    
    currentChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: trainNames,
            datasets: [{
                label: getText('engineer.stats.incidents.chart.dataset_label'),
                data: incidentCounts,
                backgroundColor: colors.background,
                borderColor: colors.border,
                borderWidth: 2
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            resizeDelay: 0,
            interaction: {
                intersect: false,
                mode: 'index'
            },
            plugins: {
                title: {
                    display: true,
                    text: getText('engineer.stats.incidents.chart.title', trainNames.length),
                    font: { size: 18, weight: 'bold' },
                    color: '#2c3e50'
                },
                legend: {
                    display: false
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return getText('engineer.stats.incidents.chart.tooltip', context.label, context.parsed.y);
                        }
                    }
                }
            },
            scales: {
                x: {
                    title: {
                        display: true,
                        text: getText('engineer.stats.incidents.chart.x_axis'),
                        color: '#2c3e50',
                        font: { size: 14, weight: 'bold' }
                    },
                    ticks: {
                        color: '#2c3e50',
                        maxRotation: 90,
                        minRotation: 45,
                        font: { size: 10 }
                    }
                },
                y: {
                    title: {
                        display: true,
                        text: getText('engineer.stats.incidents.chart.y_axis'),
                        color: '#2c3e50',
                        font: { size: 14, weight: 'bold' }
                    },
                    ticks: {
                        color: '#2c3e50',
                        beginAtZero: true,
                        stepSize: 1
                    }
                }
            }
        }
    });
    
    showNotification(getText('engineer.stats.incidents.analysis_completed'), 'success');
}

function generateChartColors(count) {
    const baseColors = [
        '#e74c3c', '#e67e22', '#f39c12', '#f1c40f', '#2ecc71', 
        '#1abc9c', '#3498db', '#9b59b6', '#34495e', '#95a5a6'
    ];
    
    const background = [];
    const border = [];
    
    for (let i = 0; i < count; i++) {
        const colorIndex = i % baseColors.length;
        background.push(baseColors[colorIndex] + '80'); // Add transparency
        border.push(baseColors[colorIndex]);
    }
    
    return { background, border };
}

function closeIncidentView() {
    const section = document.getElementById('incidentSection');
    if (section) {
        section.style.display = 'none';
    }
    
    // Destroy chart to free memory
    if (currentChart) {
        currentChart.destroy();
        currentChart = null;
    }
}

async function deleteDataset(fileId) {
    if (!confirm('Are you sure you want to delete this dataset? This action cannot be undone.')) {
        return;
    }
    
    try {
        const response = await fetch(`/engineer/api/file/${fileId}`, {
            method: 'DELETE'
        });
        
        if (response.ok) {
            showNotification('Dataset deleted successfully', 'success');
            loadDatasets(); // Refresh the list
        } else {
            showNotification('Error deleting dataset', 'error');
        }
    } catch (error) {
        console.error('Delete error:', error);
        showNotification('Error deleting dataset', 'error');
    }
}

function closeDataView() {
    const section = document.getElementById('dataViewSection');
    if (section) {
        section.style.display = 'none';
    }
}

// Utility function for notifications
function showNotification(message, type = 'info') {
    // Create toast notification
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `
        <div class="toast-content">
            <i class="fas ${getToastIcon(type)}"></i>
            <span>${message}</span>
        </div>
    `;
    
    // Add toast styles if not already added
    if (!document.getElementById('toast-styles')) {
        const styles = document.createElement('style');
        styles.id = 'toast-styles';
        styles.textContent = `
            .toast {
                position: fixed;
                top: 100px;
                right: 20px;
                padding: 15px 20px;
                border-radius: 10px;
                color: white;
                font-weight: 500;
                z-index: 10000;
                transform: translateX(400px);
                transition: transform 0.3s ease;
                box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
                max-width: 350px;
            }
            .toast-success { background: linear-gradient(135deg, #28a745, #20c997); }
            .toast-error { background: linear-gradient(135deg, #dc3545, #e74c3c); }
            .toast-warning { background: linear-gradient(135deg, #ffc107, #fd7e14); }
            .toast-info { background: linear-gradient(135deg, #17a2b8, #007bff); }
            .toast-content { display: flex; align-items: center; gap: 10px; }
            .toast.show { transform: translateX(0); }
        `;
        document.head.appendChild(styles);
    }
    
    document.body.appendChild(toast);
    
    // Show toast
    setTimeout(() => toast.classList.add('show'), 100);
    
    // Remove toast after 4 seconds
    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => document.body.removeChild(toast), 300);
    }, 4000);
}

function getToastIcon(type) {
    switch (type) {
        case 'success': return 'fa-check-circle';
        case 'error': return 'fa-exclamation-circle';
        case 'warning': return 'fa-exclamation-triangle';
        default: return 'fa-info-circle';
    }
}

// Export functions for global access
window.loadDatasets = loadDatasets;
window.loadDatasetsWithRetry = loadDatasetsWithRetry;
window.viewData = viewData;
window.showIncidents = showIncidents;
window.createIncidentChart = createIncidentChart;
window.generateChartColors = generateChartColors;
window.closeIncidentView = closeIncidentView;
window.deleteDataset = deleteDataset;
window.closeDataView = closeDataView;
window.initializeStatsPage = initializeStatsPage;
window.ensureDatasetsLoaded = ensureDatasetsLoaded;
window.checkForStuckLoading = checkForStuckLoading; 