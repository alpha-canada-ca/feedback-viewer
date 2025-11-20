function CustomDropdown(options) {

  const settings = options.settings || {};
  const events = options.events || {};

  // Get language from global variable or default to English
  const isFrench = typeof langSession !== 'undefined' && langSession === 'fr';

  // Default configuration
  const config = {
    select: options.select,
    multiSelect: false,
    searchMinChars: 2,
    debounceDelay: 800,
    hideSelected: settings.hideSelected !== false,
    keepOrder: settings.keepOrder !== false,
    closeOnSelect: settings.closeOnSelect !== false,
    placeholderText: settings.placeholderText || (isFrench ? "Sélectionner une option" : "Select an option"),
    searchText: settings.searchText || (isFrench ? "Aucun résultat trouvé" : "No results found"),
    searchPlaceholder: settings.searchPlaceholder || (isFrench ? "Recherche" : "Search"),
    searchingText: settings.searchingText || (isFrench ? "Recherche en cours..." : "Searching..."),
    searchFunction: events.search
  };

  const $originalSelect = $(config.select);

  if ($originalSelect.length === 0) {
    throw new Error('Target select element not found');
  }

  // Hide the original select
  $originalSelect.hide();

  // Create unique ID for this instance
  const instanceId = 'custom-dropdown-' + Math.random().toString(36).substr(2, 9);

  // Create dropdown structure
  const $wrapper = $('<div>').attr('id', instanceId + '-wrapper').addClass('custom-select-wrapper');
  const $display = $('<div>').addClass('custom-select-display').attr('tabindex', '0');
  const $placeholder = $('<span>').addClass('placeholder-text').text(config.placeholderText);
  const $selectedItems = $('<div>').addClass('selected-items').hide();
  const $arrow = $('<span>').addClass('dropdown-arrow').html('&#9662;');

  const $dropdown = $('<div>').addClass('custom-dropdown').hide();
  const $searchContainer = $('<div>').addClass('search-container');
  const $searchInput = $('<input>').attr({
    type: 'text',
    placeholder: config.searchPlaceholder,
    class: 'search-input',
    autocomplete: 'off'
  });
  const $statusContainer = $('<div>').addClass('status-container').hide();
  const $resultsContainer = $('<div>').addClass('results-container');

  // Assemble the structure
  $searchContainer.append($searchInput);
  $dropdown.append($searchContainer, $statusContainer, $resultsContainer);
  $display.append($placeholder, $selectedItems, $arrow);
  $wrapper.append($display, $dropdown);
  $originalSelect.after($wrapper);

  // State management
  let debounceTimer = null;
  let isDropdownOpen = false;
  let selectedValues = [];
  let lastSearchTerm = "";
  let currentResults = [];

  // Internal methods
  function updateDisplay() {
    if (selectedValues.length === 0) {
      $placeholder.show();
      $selectedItems.hide().empty();
    } else {
      $placeholder.hide();
      $selectedItems.show().empty();

      selectedValues.forEach(value => {
        const $chip = $('<span>').addClass('selected-chip').text(value);
        const $removeBtn = $('<span>').addClass('remove-chip').attr('data-value', value).html('&times;');
        $chip.append($removeBtn);
        $selectedItems.append($chip);
      });
    }

    // Update original select
    $originalSelect.empty();
    selectedValues.forEach(value => {
      $originalSelect.append(new Option(value, value, false, true));
    });
    $originalSelect.trigger('change');
  }

  function showStatus(message, className = '') {
    const $statusMsg = $('<div>').addClass('status-message').text(message);
    if (className) {
      $statusMsg.addClass(className);
    }
    $statusContainer.empty().append($statusMsg).show();
  }

  function hideStatus() {
    $statusContainer.hide().empty();
  }

  function showResults(results) {
    $resultsContainer.empty();

    if (!results || results.length === 0) {
      return;
    }

    // Filter out selected items if hideSelected is true
    let availableResults = results;
    if (config.hideSelected) {
      availableResults = results.filter(item => !selectedValues.includes(item));
    }

    if (availableResults.length === 0) {
      const $noMore = $('<div>').addClass('no-more-results').text('All matching options already selected');
      $resultsContainer.append($noMore);
      return;
    }

    availableResults.forEach(item => {
      const $option = $('<div>')
        .addClass('dropdown-option')
        .attr('data-value', item)
        .text(item);
      $resultsContainer.append($option);
    });
  }

  function performSearch(searchTerm) {
    // Handle empty search - show "No results found"
    if (!searchTerm || searchTerm.trim() === '') {
      showStatus(config.searchText, 'no-results-status');
      $resultsContainer.empty();
      return;
    }

    // Handle minimum character requirement
    if (searchTerm.length < config.searchMinChars) {
      const minCharsError = isFrench ?
        "La recherche doit comporter au moins 2 caractères" : "Search must be at least 2 characters";
      showStatus(minCharsError, 'warning-status');
      $resultsContainer.empty();
      return;
    }

    if (searchTerm === lastSearchTerm) {
      return;
    }

    lastSearchTerm = searchTerm;
    showStatus(config.searchingText, 'loading-status');
    $resultsContainer.empty();

    // Call the search function in SlimSelect format
    if (config.searchFunction) {
      // Create currentData in SlimSelect format
      const currentData = selectedValues.map(value => ({ value: value, text: value }));

      const searchPromise = config.searchFunction(searchTerm, currentData);

      if (searchPromise && typeof searchPromise.then === 'function') {
        searchPromise
          .then(options => {
            // Handle SlimSelect format response: [{ text: "...", value: "..." }]
            currentResults = options.map(option => option.text || option.value || option);

            if (currentResults.length === 0) {
              showStatus(config.searchText, 'no-results-status');
              $resultsContainer.empty();
            } else {
              // HIDE status when showing results
              hideStatus();
              showResults(currentResults);
            }
          })
          .catch(error => {
            console.error("Error in search function:", error);
            showStatus(typeof error === 'string' ? error : config.searchText, 'error-status');
            $resultsContainer.empty();
          });
      }
    }
  }

  function openDropdown() {
    if (isDropdownOpen) return;

    isDropdownOpen = true;
    $dropdown.show();
    $arrow.addClass('open');

    setTimeout(() => {
      $searchInput.focus();
    }, 50);

    $searchInput.val('');
    lastSearchTerm = '';
    hideStatus();
    $resultsContainer.empty();
  }

  function closeDropdown() {
    if (!isDropdownOpen) return;

    isDropdownOpen = false;
    $dropdown.hide();
    $arrow.removeClass('open');
    $searchInput.val('');
    lastSearchTerm = '';
    hideStatus();
    $resultsContainer.empty();
  }

  function selectValue(value) {
    if (!selectedValues.includes(value)) {
      selectedValues.push(value);
      updateDisplay();

      // Remove from current view if hideSelected is true
      if (config.hideSelected) {
        $resultsContainer.find(`[data-value="${value}"]`).remove();
        const remainingCount = $resultsContainer.find('.dropdown-option').length;
        if (remainingCount === 0 && currentResults.length > 0) {
          showStatus('All matching options selected', 'no-results-status');
        } else if (remainingCount > 0) {
          const resultsCount = isFrench ?
            `${remainingCount} résultat${remainingCount > 1 ? 's' : ''}` :
            `${remainingCount} result${remainingCount !== 1 ? 's' : ''}`;
          showStatus(resultsCount, 'results-count-status');
        }
      }

      // Close dropdown if closeOnSelect is true
      if (config.closeOnSelect) {
        closeDropdown();
      }
    }
  }

  function removeValue(value) {
    selectedValues = selectedValues.filter(v => v !== value);
    updateDisplay();

    // Re-add to results if dropdown is open
    if (isDropdownOpen && currentResults.includes(value)) {
      const $option = $('<div>')
        .addClass('dropdown-option')
        .attr('data-value', value)
        .text(value);
      $resultsContainer.append($option);

      const availableCount = $resultsContainer.find('.dropdown-option').length;
      const resultsCount = isFrench ?
        `${availableCount} résultat${availableCount > 1 ? 's' : ''}` :
        `${availableCount} result${availableCount !== 1 ? 's' : ''}`;
      showStatus(resultsCount, 'results-count-status');
    }
  }

  // Event handlers
  $display.on('click', function(e) {
    e.preventDefault();
    e.stopPropagation();
    if (isDropdownOpen) {
      closeDropdown();
    } else {
      openDropdown();
    }
  });

  $searchInput.on('input', function() {
    const searchTerm = $(this).val().trim();
    clearTimeout(debounceTimer);

    const instance = publicAPI;
    clearTimeout(instance.debounceTimer);

    instance.debounceTimer = setTimeout(() => {
      performSearch(searchTerm);
    }, config.debounceDelay);
  });

  $searchInput.on('mousedown focus', function(e) {
    e.stopPropagation();
  });

  $searchInput.on('keydown', function(e) {
    if (e.key === 'Escape') {
      e.preventDefault();
      closeDropdown();
    } else if (e.key === 'Enter') {
      e.preventDefault();
      const $firstOption = $resultsContainer.find('.dropdown-option:first');
      if ($firstOption.length > 0) {
        $firstOption.trigger('click');
      }
    }
  });

  $resultsContainer.on('click', '.dropdown-option', function(e) {
    e.preventDefault();
    e.stopPropagation();
    const value = $(this).attr('data-value');
    selectValue(value);
  });

  $selectedItems.on('click', '.remove-chip', function(e) {
    e.preventDefault();
    e.stopPropagation();
    const value = $(this).attr('data-value');
    removeValue(value);
  });

  $(document).on('click', function(e) {
    if (!$(e.target).closest(`#${instanceId}-wrapper`).length) {
      closeDropdown();
    }
  });

  $dropdown.on('click', function(e) {
    e.stopPropagation();
  });

  $display.on('keydown', function(e) {
    if (e.key === 'Enter' || e.key === ' ') {
      e.preventDefault();
      if (isDropdownOpen) {
        closeDropdown();
      } else {
        openDropdown();
      }
    } else if (e.key === 'Escape') {
      closeDropdown();
    }
  });

  // Initialize
  updateDisplay();

  const publicAPI = {
    setData: function(data) {
      currentResults = data || [];
    },
    setSelected: function(values) {
      selectedValues = Array.isArray(values) ? [...values] : [];
      updateDisplay();
    },
    getSelected: function() {
      return [...selectedValues];
    },
    close: function() {
      closeDropdown();
    },
    open: function() {
      openDropdown();
    },
    destroy: function() {
      $wrapper.remove();
      $originalSelect.show();
    },

    debounceTimer: null
  };

  return publicAPI;
}