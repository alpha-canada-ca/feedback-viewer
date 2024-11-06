$(document).ready(function () {
  var isFrench = langSession === "fr";
  var now = new Date();
  var formattedDate = now.getMonth() + 1 + "/" + now.getDate() + "/" + now.getFullYear();
  var loadingSpinner = $(".loading-spinner");

  // Utility functions
  function debounce(func, delay) {
    let debounceTimer;
    return function () {
      const context = this;
      const args = arguments;
      clearTimeout(debounceTimer);
      debounceTimer = setTimeout(() => func.apply(context, args), delay);
    };
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

  var table = $("#topTaskTable").DataTable({
    language: isFrench ? { url: "//cdn.datatables.net/plug-ins/1.10.21/i18n/French.json" } : undefined,
    processing: true,
    serverSide: true,
    retrieve: true,
    drawCallback: function () {
      fetchTotalDistinctTask();
      fetchTotalTaskCount();
    },
    ajax: {
      url: "/topTaskData",
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
          var dateRange = $("#dateRangePicker").data("daterangepicker");
          d.startDate = dateRange.startDate.format("YYYY-MM-DD");
          d.endDate = dateRange.endDate.format("YYYY-MM-DD");
        } else {
          delete d.startDate;
          delete d.endDate;
        }
        d.includeCommentsOnly = $("#commentsCheckbox").is(":checked");
      },
      error: function (xhr, error, thrown) {
        alert(isFrench ? "Erreur lors de la récupération des données. Veuillez rafraîchir la page et réessayer." : "Error retrieving data. Please refresh the page and try again.");
        console.log("xhr: " + xhr);
        console.log("error: " + error);
        console.log("thrown : " + thrown);
        loadingSpinner.hide();
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
    fetch("/topTask/totalDistinctTasks")
      .then((response) => response.text())
      .then((totalDistinctTasks) => {
        $(".stat .totalDistinctTasks").text(totalDistinctTasks);
      })
      .catch((err) => {
        console.warn("Something went wrong.", err);
      });
  }

  function fetchTotalTaskCount() {
    fetch("/topTask/totalTaskCount")
      .then((response) => response.text())
      .then((totalTaskCount) => {
        $(".stat .totalTaskCount").text(totalTaskCount);
      })
      .catch((err) => {
        console.warn("Something went wrong.", err);
      });
  }

  fetch("/topTaskSurvey/departments")
    .then(response => response.json())
    .then(departments => {
      departments.forEach(department => {
        $("#department").append(`<option value="${department.value}">${department.display}</option>`);
      });
    })
    .catch(err => {
      console.warn("Something went wrong.", err);
    });

  $("#tasks").on("change", function () {
    table.ajax.reload();
  });

  function resetFilters() {
    $("#department").val("");
    $("#theme").val("");
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
        format: "YYYY/MM/DD",
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
      $("#dateRangePicker").val(start.format("YYYY/MM/DD") + " - " + end.format("YYYY/MM/DD"));
      table.ajax.reload();
    }
  );

  $("#dateRangePicker").on("cancel.daterangepicker", function (ev, picker) {
    picker.setStartDate(moment(earliestDate));
    picker.setEndDate(moment(latestDate));
    $("#dateRangePicker").val(moment(earliestDate).format("YYYY/MM/DD") + " - " + moment(latestDate).format("YYYY/MM/DD"));
    table.ajax.reload();
  });

  $("#downloadCSV").on("click", function () {
    loadingSpinner.show();
    var url = new URL(window.location.origin + '/exportTopTaskCSV');
    url.search = getFilterParams().toString();
    
    fetch(url)
      .then(response => {
        loadingSpinner.hide();
        if (response.status === 204) {
          alert(isFrench ? "Aucune donnée à exporter avec les filtres sélectionnés." : "No data to export with the selected filters.");
          return;
        }
        if (!response.ok) {
          return response.text().then(text => {
            throw new Error(text);
          });
        }
        return response.blob();
      })
      .then(blob => {
        if (blob) {
          const url = window.URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.style.display = 'none';
          a.href = url;
          a.download = "top_task_survey_export.csv";
          document.body.appendChild(a);
          a.click();
          window.URL.revokeObjectURL(url);
        }
      })
      .catch(error => {
        loadingSpinner.hide();
        console.error('Error downloading CSV:', error);
        alert(isFrench ? 
          "Erreur lors du téléchargement du fichier CSV. Veuillez réessayer." : 
          "Error downloading CSV file. Please try again.");
      });
  });

  $("#downloadExcel").on("click", function () {
    loadingSpinner.show();
    var url = new URL(window.location.origin + '/exportTopTaskExcel');
    url.search = getFilterParams().toString();
    
    fetch(url)
      .then(response => {
        loadingSpinner.hide();
        if (response.status === 204) {
          alert(isFrench ? "Aucune donnée à exporter avec les filtres sélectionnés." : "No data to export with the selected filters.");
          return;
        }
        if (!response.ok) {
          return response.text().then(text => {
            throw new Error(text);
          });
        }
        return response.blob();
      })
      .then(blob => {
        if (blob) {
          const url = window.URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.style.display = 'none';
          a.href = url;
          a.download = "top_task_survey_export.xlsx";
          document.body.appendChild(a);
          a.click();
          window.URL.revokeObjectURL(url);
        }
      })
      .catch(error => {
        loadingSpinner.hide();
        console.error('Error downloading Excel:', error);
        alert(isFrench ? 
          "Erreur lors du téléchargement du fichier Excel. Veuillez réessayer." : 
          "Error downloading Excel file. Please try again.");
      });
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
      params.append('startDate', dateRange.startDate.format('YYYY-MM-DD'));
      params.append('endDate', dateRange.endDate.format('YYYY-MM-DD'));
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
            if (search.length < 2) {
              return reject(isFrench ? "La recherche doit comporter au moins 2 caractères" : "Search must be at least 2 characters");
            }

            fetch("/taskNames?search=" + encodeURIComponent(search), {
              method: "GET",
              headers: {
                Accept: "application/json",
              },
            })
            .then((response) => {
              if (!response.ok) {
                throw new Error(isFrench ? "La réponse du réseau n'était pas correcte" : "Network response was not ok");
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
          }, 800);
        });
      },
    },
  });
});
