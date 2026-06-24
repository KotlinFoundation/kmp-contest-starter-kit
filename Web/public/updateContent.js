function loadConfigAndUpdateContent() {
    document.querySelectorAll(".app-name").forEach(function(element) {
        element.textContent = CONFIG.APP_NAME;
    });
    document.querySelectorAll(".developer-or-company-name").forEach(function(element) {
        element.textContent = CONFIG.DEVELOPER_OR_COMPANY_NAME;
    });
    document.querySelectorAll(".contact-email").forEach(function(element) {
        element.href = "mailto:" + CONFIG.CONTACT_EMAIL;
        element.textContent = CONFIG.CONTACT_EMAIL;
    });
    document.querySelectorAll(".privacy-policy-last-update-date").forEach(function(element) {
        element.textContent = CONFIG.PRIVACY_POLICY_LAST_UPDATE_DATE;
    });
    document.querySelectorAll(".terms-and-services-last-update-date").forEach(function(element) {
        element.textContent = CONFIG.TERMS_AND_SERVICE_LAST_UPDATE_DATE;
    });
    document.querySelectorAll(".current-year").forEach(function(element) {
        element.textContent = new Date().getFullYear();
    });

    // Set website title and description
    if (window.location.pathname === "/" || window.location.pathname === "/index.html") {
        document.title = CONFIG.WEBSITE_TITLE;
        document.querySelector('meta[name="description"]').setAttribute('content', CONFIG.WEBSITE_DESCRIPTION); 
 

    }
    // Set app store and play store links
    document.querySelectorAll(".app-store-link").forEach(function(element) {
        if (CONFIG.APPSTORE_URL && CONFIG.APPSTORE_URL.trim() !== "") { 
            element.href = CONFIG.APPSTORE_URL;
            element.style.display = 'inline-block'; 

        }
    });

    document.querySelectorAll(".play-store-link").forEach(function(element) {
        if (CONFIG.PLAYSTORE_URL && CONFIG.PLAYSTORE_URL.trim() !== "") { 
            element.href = CONFIG.PLAYSTORE_URL;
            element.style.display = 'inline-block'; 

        }
    });

    // Hero Section
    document.querySelectorAll(".hero-title").forEach(function(element) {
        element.textContent = TEXT_CONTENT.HERO_TITLE;
    });
    document.querySelectorAll(".hero-subtitle").forEach(function(element) {
        element.textContent = TEXT_CONTENT.HERO_SUBTITLE;
    });

    //Problem Section
    document.querySelector('.problems-section h2').textContent = TEXT_CONTENT.PROBLEM_SECTION_TITLE;
    document.querySelector('.problems-section p').textContent = TEXT_CONTENT.PROBLEM_SECTION_TEXT;

    document.querySelector('.problems-section .card:nth-child(1) h3').textContent = TEXT_CONTENT.PROBLEM_CARD_TITLE1;
    document.querySelector('.problems-section .card:nth-child(1) p').textContent = TEXT_CONTENT.PROBLEM_CARD_TEXT1;

    document.querySelector('.problems-section .card:nth-child(2) h3').textContent = TEXT_CONTENT.PROBLEM_CARD_TITLE2;
    document.querySelector('.problems-section .card:nth-child(2) p').textContent = TEXT_CONTENT.PROBLEM_CARD_TEXT2;

    document.querySelector('.problems-section .card:nth-child(3) h3').textContent = TEXT_CONTENT.PROBLEM_CARD_TITLE3;
    document.querySelector('.problems-section .card:nth-child(3) p').textContent = TEXT_CONTENT.PROBLEM_CARD_TEXT3;



    // Feature Section
    document.querySelector('.features-section h2').textContent = TEXT_CONTENT.FEATURE_SECTION_TITLE;
    document.querySelector('.features-section p').textContent = TEXT_CONTENT.FEATURE_SECTION_TEXT;

    document.querySelector('.features-section .card:nth-child(1) h3').textContent = TEXT_CONTENT.FEATURE_CARD_TITLE1;
    document.querySelector('.features-section .card:nth-child(1) p').textContent = TEXT_CONTENT.FEATURE_CARD_TEXT1;

    document.querySelector('.features-section .card:nth-child(2) h3').textContent = TEXT_CONTENT.FEATURE_CARD_TITLE2;
    document.querySelector('.features-section .card:nth-child(2) p').textContent = TEXT_CONTENT.FEATURE_CARD_TEXT2;

    document.querySelector('.features-section .card:nth-child(3) h3').textContent = TEXT_CONTENT.FEATURE_CARD_TITLE3;
    document.querySelector('.features-section .card:nth-child(3) p').textContent = TEXT_CONTENT.FEATURE_CARD_TEXT3;

    // Call to Action Section
    document.querySelector('.cta h2').textContent = TEXT_CONTENT.CTA_SECTION_TITLE;
    document.querySelector('.cta p').textContent = TEXT_CONTENT.CTA_SECTION_TEXT;


}

document.addEventListener("DOMContentLoaded", function() {
    const script = document.createElement('script');
    script.src = 'config.js';
    script.onload = loadConfigAndUpdateContent;
    document.head.appendChild(script);
});