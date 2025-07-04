$(document).ready(function () {
  // Constants and Configuration
  const CONFIG = {
    DEBOUNCE_DELAY: 800,
    SEARCH_MIN_CHARS: 2,
    SPINNER_HIDE_DELAY: 1000,
    DATE_FORMAT: 'YYYY/MM/DD',
    BACKEND_DATE_FORMAT: 'YYYY-MM-DD'
  };

  const ENDPOINTS = {
    TOP_TASK_DATA: '/topTaskData',
    TOTAL_DISTINCT_TASKS: '/topTask/totalDistinctTasks',
    TOTAL_TASK_COUNT: '/topTask/totalTaskCount',
    DEPARTMENTS: '/topTaskSurvey/departments',
    TASK_NAMES: '/taskNames',
    EXPORT_CSV: '/exportTopTaskCSV',
    EXPORT_EXCEL: '/exportTopTaskExcel'
  };

  const MESSAGES = {
    fr: {
      ERROR_RETRIEVING_DATA: "Erreur lors de la récupération des données. Veuillez rafraîchir la page et réessayer.",
      NO_DATA_EXPORT: "Aucune donnée à exporter avec les filtres sélectionnés.",
      ERROR_CSV_DOWNLOAD: "Erreur lors du téléchargement du fichier CSV. Veuillez réessayer.",
      ERROR_EXCEL_DOWNLOAD: "Erreur lors du téléchargement du fichier Excel. Veuillez réessayer.",
      SEARCH_MIN_CHARS: "La recherche doit comporter au moins 2 caractères",
      NETWORK_ERROR: "La réponse du réseau n'était pas correcte"
    },
    en: {
      ERROR_RETRIEVING_DATA: "Error retrieving data. Please refresh the page and try again.",
      NO_DATA_EXPORT: "No data to export with the selected filters.",
      ERROR_CSV_DOWNLOAD: "Error downloading CSV file. Please try again.",
      ERROR_EXCEL_DOWNLOAD: "Error downloading Excel file. Please try again.",
      SEARCH_MIN_CHARS: "Search must be at least 2 characters",
      NETWORK_ERROR: "Network response was not ok"
    }
  };

  var isFrench = langSession === "fr";
  var now = new Date();
  var formattedDate = now.getMonth() + 1 + "/" + now.getDate() + "/" + now.getFullYear();
  var loadingSpinner = $(".loading-spinner");

  // Utility functions
  function formatNumberWithCommas(number) {
    if (number == null || number === '') return number;
    return parseInt(number).toLocaleString();
  }

  function getMessage(key) {
    return MESSAGES[isFrench ? 'fr' : 'en'][key] || MESSAGES.en[key];
  }

  function showAlert(messageKey) {
    alert(getMessage(messageKey));
  }

  function handleError(error, messageKey, context = '') {
    console.error(`Error in ${context}:`, error);
    showAlert(messageKey);
    loadingSpinner.hide();
  }

  function debounce(func, delay) {
    let debounceTimer;
    return function () {
      const context = this;
      const args = arguments;
      clearTimeout(debounceTimer);
      debounceTimer = setTimeout(() => func.apply(context, args), delay);
    };
  }

  function createDownloadLink(blob, filename) {
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.style.display = 'none';
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    window.URL.revokeObjectURL(url);
    document.body.removeChild(a);
  }

  function extractFilenameFromHeader(contentDisposition, defaultFilename) {
    if (contentDisposition && contentDisposition.indexOf('attachment') !== -1) {
      const filenameRegex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
      const matches = filenameRegex.exec(contentDisposition);
      if (matches && matches[1]) {
        return matches[1].replace(/['"]/g, '');
      }
    }
    return defaultFilename;
  }

  function getLastFiscalQuarter() {
    let today = moment();
    let fiscalYearStart = moment().month() < 3 ? moment().subtract(1, "year").month(3).startOf("month") : moment().month(3).startOf("month");
    let quarterStart, quarterEnd;

    if (today.isBetween(fiscalYearStart, fiscalYearStart.clone().add(2, "months").endOf("month"))) {
      quarterStart = fiscalYearStart.clone().subtract(1, "year").add(9, "months");
      quarterEnd = fiscalYearStart.clone().subtract(1, "day");
    } else if (today.isBefore(fiscalYearStart.clone().add(6, "months"))) {
      quarterStart = fiscalYearStart;
      quarterEnd = fiscalYearStart.clone().add(2, "months").endOf("month");
    } else if (today.isBefore(fiscalYearStart.clone().add(9, "months"))) {
      quarterStart = fiscalYearStart.clone().add(3, "months");
      quarterEnd = fiscalYearStart.clone().add(5, "months").endOf("month");
    } else {
      quarterStart = fiscalYearStart.clone().add(6, "months");
      quarterEnd = fiscalYearStart.clone().add(8, "months").endOf("month");
    }

    return [quarterStart, quarterEnd];
  }

  var table = new DataTable("#topTaskTable", {
    language: isFrench ? { url: "//cdn.datatables.net/plug-ins/2.3.2/i18n/fr-FR.json" } : undefined,
    processing: true,
    serverSide: true,
    retrieve: true,
    drawCallback: function () {
      fetchTotalDistinctTask();
      fetchTotalTaskCount();
    },
    ajax: {
      url: ENDPOINTS.TOP_TASK_DATA,
      type: "GET",
      data: function (d) {
        loadingSpinner.show();
        d.department = $("#department").val();
        d.theme = $("#theme").val();
        d.tasks = $("#tasks").val();
        d.group = $("#group").val();
        d.language = $("#language").val();
        var dateRangePickerValue = $("#dateRangePicker").val();
        if (dateRangePickerValue) {
          var dateRange = $("#dateRangePicker").data("daterangepicker");        d.startDate = dateRange.startDate.format(CONFIG.BACKEND_DATE_FORMAT);
        d.endDate = dateRange.endDate.format(CONFIG.BACKEND_DATE_FORMAT);
        } else {
          delete d.startDate;
          delete d.endDate;
        }
        d.includeCommentsOnly = $("#commentsCheckbox").is(":checked");
      },
      error: function (xhr, error, thrown) {
        handleError({xhr, error, thrown}, 'ERROR_RETRIEVING_DATA', 'DataTable AJAX');
      },
      complete: function() {
        loadingSpinner.hide();
      }
    },
    columns: [
      { data: 'dateTime' },
      { data: 'timeStamp' },
      { data: 'surveyReferrer' },
      { data: 'language' },
      { data: 'device' },
      { data: 'screener' },
      { data: 'dept' },
      { data: 'theme' },
      { data: 'themeOther' },
      { data: 'grouping' },
      { data: 'task' },
      { data: 'taskOther' },
      { data: 'taskSatisfaction' },
      { data: 'taskEase' },
      { data: 'taskCompletion' },
      { data: 'taskImprove' },
      { data: 'taskImproveComment' },
      { data: 'taskWhyNot' },
      { data: 'taskWhyNotComment' },
      { data: 'taskSampling' },
      { data: 'samplingInvitation' },
      { data: 'samplingGC' },
      { data: 'samplingCanada' },
      { data: 'samplingTheme' },
      { data: 'samplingInstitution' },
      { data: 'samplingGrouping' },
      { data: 'samplingTask' }
    ],
  });

  function fetchTotalDistinctTask() {
    fetch(ENDPOINTS.TOTAL_DISTINCT_TASKS)
      .then((response) => {
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.text();
      })
      .then((totalDistinctTasks) => {
        $(".stat .totalDistinctTasks").text(formatNumberWithCommas(totalDistinctTasks));
      })
      .catch((err) => {
        console.warn("Error fetching total distinct tasks:", err);
      });
  }

  function fetchTotalTaskCount() {
    fetch(ENDPOINTS.TOTAL_TASK_COUNT)
      .then((response) => {
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.text();
      })
      .then((totalTaskCount) => {
        $(".stat .totalTaskCount").text(formatNumberWithCommas(totalTaskCount));
      })
      .catch((err) => {
        console.warn("Error fetching total task count:", err);
      });
  }

  fetch(ENDPOINTS.DEPARTMENTS)
    .then(response => {
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      return response.json();
    })
    .then(departments => {
      const departmentSelect = $("#department");
      departments.forEach(department => {
        departmentSelect.append(`<option value="${department.value}">${department.display}</option>`);
      });
    })
    .catch(err => {
      console.warn("Error fetching departments:", err);
    });

  $("#tasks").on("change", function () {
    table.ajax.reload();
  });

  function resetFilters() {
    $("#department").val("");
    $("#theme").val("");
    $("#group").val("");
    $("#language").val("");
    taskSelect.setData([]);
    taskSelect.setSelected([]);
    $("#tasks").val("");
    $("#dateRangePicker").data("daterangepicker").setStartDate(moment(earliestDate));
    $("#dateRangePicker").data("daterangepicker").setEndDate(moment(latestDate));
    $("#dateRangePicker").val(earliestDate + " - " + latestDate);
    $("#commentsCheckbox").prop("checked", false);
    table.ajax.reload();
  }

  $(".reset-filters").on("click", resetFilters);

  $("#dateRangePicker").daterangepicker(
    {
      opens: "left",
      startDate: moment(earliestDate),
      endDate: moment(latestDate),
      minDate: moment(earliestDate),
      maxDate: moment(latestDate),
      alwaysShowCalendars: true,
      locale: {
        format: CONFIG.DATE_FORMAT,
        cancelLabel: isFrench ? "Effacer" : "Clear",
        applyLabel: isFrench ? "Appliquer" : "Apply",
        customRangeLabel: isFrench ? "Période spécifique" : "Custom Range",
        firstDay: isFrench ? 1 : 0,
        daysOfWeek: isFrench ? ["Dim", "Lun", "Mar", "Mer", "Jeu", "Ven", "Sam"] : undefined,
        monthNames: isFrench ? ["Janvier", "Février", "Mars", "Avril", "Mai", "Juin", "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"] : undefined,
      },
      ranges: {
        [isFrench ? "Toutes les dates" : "All Dates"]: [moment(earliestDate), moment(latestDate)],
        [isFrench ? "Aujourd'hui" : "Today"]: [moment(), moment()],
        [isFrench ? "Hier" : "Yesterday"]: [moment().subtract(1, "days"), moment().subtract(1, "days")],
        [isFrench ? "7 derniers jours" : "Last 7 Days"]: [moment().subtract(6, "days"), moment()],
        [isFrench ? "30 derniers jours" : "Last 30 Days"]: [moment().subtract(29, "days"), moment()],
        [isFrench ? "Ce mois-ci" : "This Month"]: [moment().startOf("month"), moment().endOf("month")],
        [isFrench ? "Le mois dernier" : "Last Month"]: [moment().subtract(1, "month").startOf("month"), moment().subtract(1, "month").endOf("month")],
        [isFrench ? "Dernier trimestre" : "Last Quarter"]: getLastFiscalQuarter(),
      },
    },
    function (start, end, label) {
      $("#dateRangePicker").val(start.format(CONFIG.DATE_FORMAT) + " - " + end.format(CONFIG.DATE_FORMAT));
      table.ajax.reload();
    }
  );

  $("#dateRangePicker").on("cancel.daterangepicker", function (ev, picker) {
    picker.setStartDate(moment(earliestDate));
    picker.setEndDate(moment(latestDate));
    $("#dateRangePicker").val(moment(earliestDate).format(CONFIG.DATE_FORMAT) + " - " + moment(latestDate).format(CONFIG.DATE_FORMAT));
    table.ajax.reload();
  });

  function handleDownload(url, defaultFilename, errorMessageKey) {
    loadingSpinner.show();
    
    fetch(url)
      .then(response => {
        if (response.status === 204) {
          loadingSpinner.hide();
          showAlert('NO_DATA_EXPORT');
          return null;
        }
        if (!response.ok) {
          return response.text().then(text => {
            throw new Error(text);
          });
        }
        
        const disposition = response.headers.get('Content-Disposition');
        const filename = extractFilenameFromHeader(disposition, defaultFilename);
        return { blob: response.blob(), filename };
      })
      .then(result => {
        if (result && result.blob) {
          result.blob.then(blob => {
            createDownloadLink(blob, result.filename);
            setTimeout(() => {
              loadingSpinner.hide();
            }, CONFIG.SPINNER_HIDE_DELAY);
          });
        }
      })
      .catch(error => {
        handleError(error, errorMessageKey, 'File download');
      });
  }

  $("#downloadCSV").on("click", function () {
    const url = new URL(window.location.origin + ENDPOINTS.EXPORT_CSV);
    url.search = getFilterParams().toString();
    handleDownload(url, 'top_task_survey_export.csv', 'ERROR_CSV_DOWNLOAD');
  });

  $("#downloadExcel").on("click", function () {
    const url = new URL(window.location.origin + ENDPOINTS.EXPORT_EXCEL);
    url.search = getFilterParams().toString();
    handleDownload(url, 'top_task_survey_export.xlsx', 'ERROR_EXCEL_DOWNLOAD');
  });
  tippy("#theme-tool-tip", {
    content: isFrench ? "Thèmes de navigation de Canada.ca " : "Canada.ca navigation themes ",
  });

  $("#department, #theme, #commentsCheckbox, #group, #language").on("change", function () {
    table.ajax.reload();
  });

  function getFilterParams() {
    var tasks = $("#tasks").val();
    var params = new URLSearchParams();
    
    params.append('department', $("#department").val() || '');
    params.append('theme', $("#theme").val() || '');
    params.append('group', $("#group").val() || '');
    params.append('language', $("#language").val() || '');
    params.append('includeCommentsOnly', $("#commentsCheckbox").is(":checked"));
  
    if (tasks && tasks.length > 0) {
      tasks.forEach(function(task) {
        params.append('tasks[]', task);
      });
    }
  
    var dateRangePickerValue = $("#dateRangePicker").val();
    if (dateRangePickerValue) {
      var dateRange = $("#dateRangePicker").data('daterangepicker');
      params.append('startDate', dateRange.startDate.format(CONFIG.BACKEND_DATE_FORMAT));
      params.append('endDate', dateRange.endDate.format(CONFIG.BACKEND_DATE_FORMAT));
    }
  
    return params;
  }

  var taskSelect = new SlimSelect({
    select: "#tasks",
    settings: {
      hideSelected: true,
      keepOrder: true,
      placeholderText: isFrench ? "Filtrer par mot-clé de la tâche" : "Filter by task keyword",
      searchText: isFrench ? "Aucun résultat trouvé" : "No results found",
      searchPlaceholder: isFrench ? "Recherche" : "Search",
      searchingText: isFrench ? "Recherche en cours..." : "Searching...",
      closeOnSelect: false,
    },
    events: {
      search: (search, currentData) => {
        return new Promise((resolve, reject) => {
          clearTimeout(taskSelect.debounceTimer);
          taskSelect.debounceTimer = setTimeout(() => {
            if (search.length < CONFIG.SEARCH_MIN_CHARS) {
              return reject(getMessage('SEARCH_MIN_CHARS'));
            }

            fetch(`${ENDPOINTS.TASK_NAMES}?search=${encodeURIComponent(search)}`, {
              method: "GET",
              headers: {
                Accept: "application/json",
              },
            })
            .then((response) => {
              if (!response.ok) {
                throw new Error(getMessage('NETWORK_ERROR'));
              }
              return response.json();
            })
            .then((data) => {
              const options = data
                .filter((title) => {
                  return !currentData.some((optionData) => optionData.value === title);
                })
                .map((title) => {
                  return { text: title, value: title };
                });

              resolve(options);
            })
            .catch((error) => {
              console.error("Error fetching page titles:", error);
              reject(error);
            });
          }, CONFIG.DEBOUNCE_DELAY);
        });
      },
    },
  });
});
