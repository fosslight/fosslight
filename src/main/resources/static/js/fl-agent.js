// Global function to fetch SBOM guide using RAG
window.fetchSbomGuide = function(ossName, ossVersion, message, buttonElement) {
  const container = buttonElement.parentElement;
  
  // Helper function to escape HTML
  function escapeHtml(text) {
    if (!text) return '';
    return text
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
  }
  
  // Show loading animation
  container.innerHTML = `
    <div style="display: flex; align-items: center; gap: 8px;">
      <div style="width: 16px; height: 16px; border: 2px solid #007bff; border-top-color: transparent; border-radius: 50%; animation: spin 1s linear infinite;"></div>
      <span style="color: #007bff; font-size: 11px;">Searching for guide...</span>
    </div>
    <style>
      @keyframes spin {
        to { transform: rotate(360deg); }
      }
    </style>
  `;
  
  // Send request to Agent server for RAG search
  console.log('[fetchSbomGuide] Starting request for:', ossName, ossVersion, message);
  fetch('/api/agent/sbom-guide', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      ossName: ossName,
      ossVersion: ossVersion,
      message: message,
      summary: true  // Request summarized guide
    })
  })
  .then(response => {
    console.log('[fetchSbomGuide] Response status:', response.status);
    if (!response.ok) {
      throw new Error('HTTP error! status: ' + response.status);
    }
    return response.json();
  })
  .then(data => {
    console.log('[fetchSbomGuide] Response data:', data);
    if (data.guide) {
      // Display guide without internal scroll
      container.innerHTML = '<div style="font-size: 11px; color: #333; line-height: 1.6; white-space: pre-wrap; overflow: visible;">' + escapeHtml(data.guide) + '</div>';
    } else {
      container.innerHTML = '<div style="color: #666; font-size: 11px;">Guide not found.</div>';
    }
  })
  .catch(error => {
    console.error('[fetchSbomGuide] Error:', error);
    container.innerHTML = '<div style="color: #dc3545; font-size: 11px;">Error occurred while searching for guide: ' + escapeHtml(error.message) + '</div>';
  });
};

(function(){
  'use strict';

  // Check if current page is a license page
  function isLicensePage() {
    const currentPath = window.location.pathname;
    return currentPath.toLowerCase().includes('license');
  }

  // Initialize after DOM is ready to ensure fragment elements exist (fix: scripts in head)
  document.addEventListener('DOMContentLoaded', function(){
    console.log('[fl-agent] DOMContentLoaded - init');
    
    // Only initialize on license pages
    if (!isLicensePage()) {
      console.log('[fl-agent] Not a license page, skipping agent initialization');
      return;
    }
    
    console.log('[fl-agent] License page detected, initializing agent');
    
    const overlay = document.getElementById('flAgentSidebarOverlay');
    const sidebar = document.getElementById('flAgentSidebar');
    
    // Hide SBOM button on page load (default state)
    const sbomAnalysisBtn = document.getElementById('flAgentSbomAnalysisBtn');
    if (sbomAnalysisBtn) {
      sbomAnalysisBtn.style.display = 'none';
      console.log('[fl-agent] SBOM button hidden on page load');
    }
    
    // Set default mode to guide
    setMode('guide');
    
    // Track current URL to detect page navigation
    let currentUrl = window.location.href;
    
    // Function to hide SBOM button and return to guide mode
    function resetToGuideMode() {
      console.log('[fl-agent] Page navigation detected, resetting to guide mode');
      const sbomAnalysisBtn = document.getElementById('flAgentSbomAnalysisBtn');
      if (sbomAnalysisBtn) {
        sbomAnalysisBtn.style.display = 'none';
      }
      
      // Clear SBOM analysis container content
      const sbomAnalysisContainer = document.getElementById('flAgentSbomAnalysisContainer');
      if (sbomAnalysisContainer) {
        sbomAnalysisContainer.innerHTML = '';
      }
      
      setMode('guide');
    }
    
    // Detect URL changes (page navigation)
    function checkUrlChange() {
      if (window.location.href !== currentUrl) {
        currentUrl = window.location.href;
        // Check if still on license page
        if (!isLicensePage()) {
          console.log('[fl-agent] Navigated away from license page, hiding agent');
          if (sidebar) {
            closeSidebar();
            sidebar.style.display = 'none';
          }
          if (overlay) {
            overlay.style.display = 'none';
          }
        } else {
          resetToGuideMode();
        }
      }
    }
    
    // Check URL changes periodically (for traditional page navigation)
    setInterval(checkUrlChange, 500);
    
    // Also listen to popstate (back/forward button)
    window.addEventListener('popstate', function() {
      checkUrlChange();
    });

    const toggle = document.getElementById('flAgentSidebarToggle');
    const closeBtn = document.getElementById('flAgentSidebarClose');

    function openSidebar(){
      if(!sidebar || !toggle || !overlay) return;
  console.log('[fl-agent] openSidebar called');
  sidebar.classList.remove('closed');
  toggle.classList.add('open');
  overlay.style.display = 'block';
    }
    function closeSidebar(){
      if(!sidebar || !toggle || !overlay) return;
      sidebar.classList.add('closed');
      toggle.classList.remove('open');
      overlay.style.display = 'none';
    }

    if(toggle){
      toggle.addEventListener('click', function(e){
        if(sidebar && sidebar.classList.contains('open')) closeSidebar(); else openSidebar();
      });
    }
    if(closeBtn) closeBtn.addEventListener('click', closeSidebar);
    if(overlay) overlay.addEventListener('click', closeSidebar);

    // mode buttons
    function setMode(mode){
      console.log('[fl-agent] setMode called with mode:', mode);
      document.querySelectorAll('.fl-agent-mode-btn').forEach(b=>b.classList.remove('active'));
      const guideBtn = document.getElementById('flAgentGuideModeBtn');
      const chatBtn = document.getElementById('flAgentChatModeBtn');
      const sbomAnalysisBtn = document.getElementById('flAgentSbomAnalysisBtn');
      const licenseBtn = document.getElementById('flAgentLicenseModeBtn');
      if(guideBtn) guideBtn.classList.toggle('active', mode==='guide');
      if(chatBtn) chatBtn.classList.toggle('active', mode==='chat');
      if(sbomAnalysisBtn) sbomAnalysisBtn.classList.toggle('active', mode==='sbomAnalysis');
      if(licenseBtn) licenseBtn.classList.toggle('active', mode==='license');

      const guideContent = document.getElementById('flAgentGuideContent');
      const chatContainer = document.getElementById('flAgentChatContainer');
      const sbomAnalysisContainer = document.getElementById('flAgentSbomAnalysisContainer');
      const licenseContainer = document.getElementById('flAgentLicenseContainer');
      
      // Hide all containers first
      if(guideContent) guideContent.style.display = 'none';
      if(chatContainer) {
        chatContainer.classList.remove('active');
        chatContainer.style.display = 'none';
      }
      if(sbomAnalysisContainer) sbomAnalysisContainer.style.display = 'none';
      if(licenseContainer) licenseContainer.style.display = 'none';
      
      // Show only the selected mode container
      if(mode==='guide' && guideContent) guideContent.style.display = 'block';
      if(mode==='chat' && chatContainer) {
        chatContainer.classList.add('active');
        chatContainer.style.display = 'block';
      }
      if(mode==='sbomAnalysis' && sbomAnalysisContainer) sbomAnalysisContainer.style.display = 'block';
      if(mode==='license' && licenseContainer) licenseContainer.style.display = 'block';
    }

    document.addEventListener('click', function(e){
      if(e.target && e.target.id==='flAgentGuideModeBtn') setMode('guide');
      if(e.target && e.target.id==='flAgentChatModeBtn') setMode('chat');
      if(e.target && e.target.id==='flAgentSbomAnalysisBtn') {
        console.log('[fl-agent] SBOM Analysis button clicked');
        setMode('sbomAnalysis');
        // Trigger SBOM analysis in the iframe using postMessage
        try {
          // Find the active iframe in the tab-content
          const activeTabPane = document.querySelector('.tab-pane.active');
          console.log('[fl-agent] activeTabPane:', activeTabPane);
          if(activeTabPane) {
            const iframe = activeTabPane.querySelector('iframe');
            console.log('[fl-agent] iframe:', iframe);
            if(iframe && iframe.contentWindow) {
              console.log('[fl-agent] Sending postMessage to iframe');
              iframe.contentWindow.postMessage({type: 'sbomAnalysisRequest'}, window.location.origin);
            } else {
              console.error('[fl-agent] iframe or iframe.contentWindow not found');
            }
          } else {
            console.error('[fl-agent] No active tab pane found');
          }
        } catch(err) {
          console.error('[fl-agent] Error sending postMessage:', err);
        }
      }
      if(e.target && e.target.id==='flAgentLicenseModeBtn') setMode('license');
    });

    // chat send
    const sendBtn = document.getElementById('flAgentChatSend');
    if(sendBtn){
      sendBtn.addEventListener('click', function(){
        const txtEl = document.getElementById('flAgentChatInput');
        const txt = txtEl? txtEl.value.trim() : '';
        if(!txt) return; // noop
        appendMessage('user', txt);
        // call proxy chat endpoint
        fetch('/api/agent/chat', {method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify({message:txt})})
          .then(r=>r.json())
          .then(data=>{
            appendMessage('assistant', data && data.response? data.response : JSON.stringify(data), true);
          }).catch(err=>{
            appendMessage('assistant', '에러가 발생했습니다.');
            console.error('fl-agent chat err', err);
          });
        if(txtEl) txtEl.value = '';
      });
    }

    function appendMessage(who, text, typingEffect = false){
      const wrap = document.getElementById('flAgentChatMessages');
      if(!wrap) return;
      const div = document.createElement('div');
      div.className = 'fl-agent-chat-message ' + (who==='user'?'user':'assistant');
      const bubble = document.createElement('div');
      bubble.className = 'fl-agent-chat-bubble';
      const ts = document.createElement('span');
      ts.className = 'fl-agent-chat-timestamp';
      ts.textContent = who==='user'? '나' : 'FOSSLight AI';
      div.appendChild(bubble);
      div.appendChild(ts);
      wrap.appendChild(div);
      
      if(typingEffect && who==='assistant'){
        // Typing effect for assistant messages
        console.log('[fl-agent] Starting typing effect for:', text);
        let index = 0;
        const typingSpeed = 30; // milliseconds per character
        bubble.textContent = '';
        
        function typeChar() {
          if(index < text.length){
            bubble.textContent += text.charAt(index);
            index++;
            wrap.scrollTop = wrap.scrollHeight;
            setTimeout(typeChar, typingSpeed);
          }
        }
        typeChar();
      } else {
        // Immediate display for user messages or when typing effect is disabled
        bubble.textContent = text;
        wrap.scrollTop = wrap.scrollHeight;
      }
    }

    // quick ping check
    function pingAgent(){
      fetch('/api/agent/ping').then(r=>r.text()).then(t=>{}).catch(()=>{});
    }
    setInterval(pingAgent, 30000);
    pingAgent();

    // Open sidebar automatically on pages that include the fragment (i.e., logged-in pages)
    // small timeout so layout and other scripts settle
    if (sidebar && toggle) {
      setTimeout(function(){ try{ openSidebar(); }catch(e){console.error('fl-agent open err', e);} }, 200);
    }
    
    // Function to handle license detail tab activation
    function handleLicenseTabActivation(tabId) {
      console.log('[fl-agent] License detail tab activated:', tabId);
      
      // Wait a bit for the tab content to load, then open sidebar
      setTimeout(function() {
        try {
          openSidebar();
          setMode('license');
          console.log('[fl-agent] Agent sidebar opened for license detail tab');
        } catch(err) {
          console.error('[fl-agent] Error opening sidebar for license tab:', err);
        }
      }, 300);
    }
    
    // Listen for tab activation events to show agent sidebar on license detail pages
    document.addEventListener('click', function(e) {
      // Check if a tab link was clicked
      const tabLink = e.target.closest('.nav-link[data-toggle="row"]');
      if (tabLink) {
        const tabId = tabLink.id;
        console.log('[fl-agent] Tab clicked:', tabId);
        
        // Check if this is a license detail tab (tab ID contains "License")
        if (tabId && tabId.includes('License')) {
          handleLicenseTabActivation(tabId);
        }
      }
    });
    
    // Also observe DOM changes to detect when new tabs are added
    const tabObserver = new MutationObserver(function(mutations) {
      mutations.forEach(function(mutation) {
        if (mutation.addedNodes) {
          mutation.addedNodes.forEach(function(node) {
            // Check if a new tab pane was added
            if (node.nodeType === 1 && node.classList && node.classList.contains('tab-pane')) {
              const tabId = node.id;
              console.log('[fl-agent] New tab pane added:', tabId);
              
              // Check if this is a license detail tab
              if (tabId && tabId.includes('License')) {
                // Find the corresponding tab link
                const tabLink = document.getElementById('tab--' + tabId.replace('panel--', ''));
                if (tabLink && tabLink.classList.contains('active')) {
                  console.log('[fl-agent] License detail tab is active, opening agent sidebar');
                  handleLicenseTabActivation(tabId);
                }
              }
            }
          });
        }
      });
    });
    
    // Start observing the tab content area
    const tabContent = document.querySelector('.tab-content');
    if (tabContent) {
      tabObserver.observe(tabContent, { childList: true });
    }

    // If the fragment is inserted later via AJAX/template replacement, observe DOM mutations and open when available
    if (!sidebar) {
      console.log('[fl-agent] sidebar not present at load, setting up MutationObserver');
      const observer = new MutationObserver(function(mutations, obs){
        const sb = document.getElementById('flAgentSidebar');
        const to = document.getElementById('flAgentSidebarToggle');
        const ov = document.getElementById('flAgentSidebarOverlay');
        if (sb && to && ov) {
          console.log('[fl-agent] detected fragment insertion via MutationObserver');
          // initialize variables used by this scope
          try{ openSidebar(); } catch(e){ console.error('fl-agent open err (observer)', e); }
          obs.disconnect();
        }
      });
      observer.observe(document.documentElement || document.body, { childList:true, subtree:true });
    }

    // Guide: field focus -> request guide from proxy
    let guideTimer = null;
    const GUIDE_DEBOUNCE_MS = 350;

    function requestGuideForField(fieldName){
      const payload = { current_field: fieldName };
      // show loading
      const guideContainer = document.getElementById('flAgentGuideContent');
      if(guideContainer){
        guideContainer.innerHTML = '<div class="fl-agent-guide-card">Loading...</div>';
      }
      fetch('/api/agent/guide', { method: 'POST', headers: {'Content-Type':'application/json'}, body: JSON.stringify(payload) })
        .then(async r => {
          const text = await r.text();
          try{ return JSON.parse(text); } catch(e){ return text; }
        })
        .then(data => {
          renderGuideResponse(data);
        }).catch(err => {
          console.error('guide req err', err);
          if(guideContainer) guideContainer.innerHTML = '<div class="fl-agent-guide-card">Failed to load guide.</div>';
        });
    }

    function renderGuideResponse(data){
      const guideContainer = document.getElementById('flAgentGuideContent');
      if(!guideContainer) return;
      // If data is string, show raw
      if(typeof data === 'string'){
        guideContainer.innerHTML = '<div class="fl-agent-guide-card">'+escapeHtml(data)+'</div>';
        return;
      }
      // Common expected shape: { field, title, guide }
      const title = data.title || data.field || 'Guide';
      const guideText = data.guide || data.message || JSON.stringify(data);
      let html = '<div class="fl-agent-guide-card">';
      html += '<h3>'+escapeHtml(title)+'</h3>';
      html += '<p style="white-space:pre-wrap;">'+escapeHtml(guideText)+'</p>';
      html += '</div>';
      guideContainer.innerHTML = html;
    }

    function escapeHtml(s){
      if(s==null) return '';
      return String(s).replace(/&/g,'&').replace(/</g,'<').replace(/>/g,'>').replace(/"/g,'"').replace(/'/g,'&#039;');
    }

    // attach focus listeners to inputs and textareas (delegated)
    document.addEventListener('focusin', function(e){
      const el = e.target;
      if(!el) return;
      if(el.tagName === 'INPUT' || el.tagName === 'TEXTAREA' || el.isContentEditable){
        // Skip if the focused element is inside the agent sidebar
        const agentSidebar = el.closest('#flAgentSidebar');
        if(agentSidebar) return;
        
        const name = el.getAttribute('name') || el.id || el.getAttribute('data-field') || el.placeholder || 'field';
        // open sidebar automatically in guide mode
        setMode('guide');
        openSidebar();
        if(guideTimer) clearTimeout(guideTimer);
        guideTimer = setTimeout(()=> requestGuideForField(name), GUIDE_DEBOUNCE_MS);
      }
    });

    // Listen for SBOM analysis results from iframe
    window.addEventListener('message', function(event) {
      if (event.data && event.data.type === 'sbomAnalysisResult') {
        renderSbomAnalysisResult(event.data.data);
      }
    });

    function renderSbomAnalysisResult(warningItems) {
      const container = document.getElementById('flAgentSbomAnalysisContainer');
      if (!container) return;

      if (!warningItems || warningItems.length === 0) {
        container.innerHTML = '<div class="fl-agent-guide-card"><h3>📊 SBOM Analysis</h3><p>No warnings to analyze.</p></div>';
        return;
      }

      let html = '<div class="fl-agent-guide-card"><h3>📊 SBOM 분석 결과</h3>';
      html += '<p style="font-size: 13px; color: #333; margin-bottom: 15px;">Warnings found in ' + warningItems.length + ' items.</p>';
      
      html += '<div style="max-height: calc(100vh - 250px); overflow-y: auto; padding-right: 5px;">';
      
      // Create ticket-style cards
      for (var i = 0; i < warningItems.length; i++) {
        var item = warningItems[i];
        
        html += '<div style="background: #fff; border: 1px solid #dee2e6; border-left: 4px solid #007bff; border-radius: 8px; padding: 16px; margin-bottom: 16px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">';
        
        // Header: OSS Name and Version
        html += '<div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; padding-bottom: 12px; border-bottom: 1px solid #eee;">';
        html += '<div style="flex: 1;">';
        html += '<div style="font-size: 14px; font-weight: 600; color: #333; margin-bottom: 4px;">' + escapeHtml(item.ossName) + '</div>';
        html += '<div style="font-size: 12px; color: #666;">Version: ' + escapeHtml(item.ossVersion) + '</div>';
        html += '</div>';
        if (item.licenseName) {
          html += '<div style="background: #f0f0f0; padding: 4px 8px; border-radius: 4px; font-size: 11px; color: #333;">' + escapeHtml(item.licenseName) + '</div>';
        }
        html += '</div>';
        
        // Messages
        if (item.messages && item.messages.length > 0) {
          html += '<div style="margin-bottom: 12px;">';
          html += '<div style="font-size: 12px; font-weight: 600; color: #333; margin-bottom: 8px;">⚠️ Warning Messages:</div>';
          for (var j = 0; j < item.messages.length; j++) {
            var msg = item.messages[j];
            var color = msg.color === 'red' ? 'red' : (msg.color === 'blue' ? 'blue' : 'gray');
            var bgColor = msg.color === 'red' ? 'rgba(255, 0, 0, 0.15)' : (msg.color === 'blue' ? 'rgba(0, 0, 255, 0.15)' : 'rgba(128, 128, 128, 0.15)');
            html += '<div style="font-size: 11px; color: ' + color + '; margin-bottom: 6px; padding: 6px 10px; background: ' + bgColor + '; border-radius: 4px;">';
            html += '<strong>' + escapeHtml(msg.field) + ':</strong> ' + escapeHtml(msg.message);
            html += '</div>';
          }
          html += '</div>';
        }
        
        // Action Guide (placeholder for RAG)
        html += '<div style="background: #f8f9fa; border-radius: 6px; padding: 12px;">';
        html += '<div style="font-size: 12px; font-weight: 600; color: #333; margin-bottom: 8px;">💡 Action Guide:</div>';
        html += '<div style="font-size: 11px; color: #666; line-height: 1.5;">';
        html += '<button onclick="fetchSbomGuide(\'' + escapeHtml(item.ossName) + '\', \'' + escapeHtml(item.ossVersion) + '\', \'' + escapeHtml(item.messages[0] ? item.messages[0].message : '') + '\', this)" style="background: #007bff; color: #fff; border: none; padding: 6px 12px; border-radius: 4px; font-size: 11px; cursor: pointer;">Search Guide</button>';
        html += '</div>';
        html += '</div>';
        
        html += '</div>';
      }
      
      html += '</div>';
      html += '</div>';
      
      container.innerHTML = html;
    }

    // Global function to show license translate mode (called from iframe)
    window.flAgent = window.flAgent || {};
    window.flAgent.showLicenseTranslateMode = function(licenseName, licenseText) {
      console.log('[fl-agent] showLicenseTranslateMode called:', licenseName, licenseText);
      
      // Open sidebar
      openSidebar();
      
      // Set mode to license
      setMode('license');
      
      // Display license info in license container
      const licenseContainer = document.getElementById('flAgentLicenseContainer');
      if (licenseContainer) {
        let html = '<div class="fl-agent-guide-card">';
        html += '<h3>📄 License Translation</h3>';
        if (licenseName) {
          html += '<div style="margin-bottom: 12px;"><strong>License Name:</strong> ' + escapeHtml(licenseName) + '</div>';
        }
        if (licenseText) {
          html += '<div style="margin-bottom: 12px;"><strong>License Text:</strong></div>';
          html += '<div style="background: #f8f9fa; padding: 12px; border-radius: 6px; font-size: 11px; line-height: 1.5; max-height: 300px; overflow-y: auto;">';
          html += escapeHtml(licenseText);
          html += '</div>';
        }
        html += '<div style="margin-top: 12px; color: #666; font-size: 11px;">';
        html += 'License translation feature is under preparation.';
        html += '</div>';
        html += '</div>';
        licenseContainer.innerHTML = html;
      }
      
      console.log('[fl-agent] License translate mode activated');
    };

    // Global function to hide license translate mode
    window.flAgent.hideLicenseTranslateMode = function() {
      console.log('[fl-agent] hideLicenseTranslateMode called');
      setMode('guide');
    };
  });

})();