$(document).ready(function () {
  var isFrench = langSession === "fr";
  var now = new Date();
  var formattedDate = now.getMonth() + 1 + "/" + now.getDate() + "/" + now.getFullYear();

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
  function newexportaction(e, dt, button, config) {
    var self = this;
    var oldStart = dt.settings()[0]._iDisplayStart;
    dt.one("preXhr", function (e, s, data) {
      // Just this once, load all data from the server...
      data.start = 0;
      data.length = 2147483647;
      dt.one("preDraw", function (e, settings) {
        if (button[0].className.indexOf("buttons-excel") >= 0) {
          $.fn.dataTable.ext.buttons.excelHtml5.available(dt, config) ? $.fn.dataTable.ext.buttons.excelHtml5.action.call(self, e, dt, button, config) : $.fn.dataTable.ext.buttons.excelFlash.action.call(self, e, dt, button, config);
        } else if (button[0].className.indexOf("buttons-csv") >= 0) {
          $.fn.dataTable.ext.buttons.csvHtml5.available(dt, config) ? $.fn.dataTable.ext.buttons.csvHtml5.action.call(self, e, dt, button, config) : $.fn.dataTable.ext.buttons.csvFlash.action.call(self, e, dt, button, config);
        }
        dt.one("preXhr", function (e, s, data) {
          // DataTables thinks the first item displayed is index 0, but we're not drawing that.
          // Set the property to what it was before exporting.
          settings._iDisplayStart = oldStart;
          data.start = oldStart;
        }); // Reload the grid with the original page. Otherwise, API functions like table.cell(this) don't work properly.
        setTimeout(dt.ajax.reload, 0); // Prevent rendering of the full data to the DOM
        return false;
      });
    }); // Requery the server with the new one-time export settings
    dt.ajax.reload();
  }

  function getLastFiscalQuarter() {
    let today = moment();
    let fiscalYearStart = moment().month() < 3 ? moment().subtract(1, "year").month(3).startOf("month") : moment().month(3).startOf("month"); // Adjust based on fiscal year starting in April
    let quarterStart, quarterEnd;

    // Determine the current fiscal quarter
    if (today.isBetween(fiscalYearStart, fiscalYearStart.clone().add(2, "months").endOf("month"))) {
      // Last quarter is Q4 of the previous fiscal year
      quarterStart = fiscalYearStart.clone().subtract(1, "year").add(9, "months");
      quarterEnd = fiscalYearStart.clone().subtract(1, "day");
    } else if (today.isBefore(fiscalYearStart.clone().add(6, "months"))) {
      // Last quarter is Q1
      quarterStart = fiscalYearStart;
      quarterEnd = fiscalYearStart.clone().add(2, "months").endOf("month");
    } else if (today.isBefore(fiscalYearStart.clone().add(9, "months"))) {
      // Last quarter is Q2
      quarterStart = fiscalYearStart.clone().add(3, "months");
      quarterEnd = fiscalYearStart.clone().add(5, "months").endOf("month");
    } else {
      // Last quarter is Q3
      quarterStart = fiscalYearStart.clone().add(6, "months");
      quarterEnd = fiscalYearStart.clone().add(8, "months").endOf("month");
    }

    return [quarterStart, quarterEnd];
  }
  var loadingSpinner = $(".loading-spinner");

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
        var dateRangePickerValue = $("#dateRangePicker").val();
        if (dateRangePickerValue) {
          var dateRange = $("#dateRangePicker").data("daterangepicker");
          d.startDate = dateRange.startDate.format("YYYY-MM-DD");
          d.endDate = dateRange.endDate.format("YYYY-MM-DD");
        } else {
          // If the date range picker is empty, do not send startDate and endDate in the request
          delete d.startDate; // Ensure startDate is not included in the AJAX request
          delete d.endDate; // Ensure endDate is not included in the AJAX request
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

    buttons: [
      {
        extend: "csvHtml5",
        className: "btn btn-default",
        exportOptions: {
          modifier: {
            page: "all", // This tells DataTables to export data from all pages, not just the current page
          },
        },
        action: newexportaction,
        filename: (isFrench ? "Retroaction_du_sondage_SRT-" : "TSS_survey_feedback-") + new Date().getFullYear() + "-" + ("0" + (new Date().getMonth() + 1)).slice(-2) + "-" + ("0" + new Date().getDate()).slice(-2),
      },
      {
        extend: "excelHtml5",
        className: "btn btn-default",
        exportOptions: {
          modifier: {
            page: "all", // This tells DataTables to export data from all pages, not just the current page
          },
        },
        action: newexportaction,
        filename: (isFrench ? "Retroaction_du_sondage_SRT-" : "TSS_survey_feedback-") + new Date().getFullYear() + "-" + ("0" + (new Date().getMonth() + 1)).slice(-2) + "-" + ("0" + new Date().getDate()).slice(-2),
      },
    ],

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
        // Update the total comments count in the <span class="number"> element
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
        // Update the total comments count in the <span class="number"> element
        $(".stat .totalTaskCount").text(totalTaskCount);
      })
      .catch((err) => {
        console.warn("Something went wrong.", err);
      });
  }
  fetch("/topTaskSurvey/departments")
  .then(response => response.json()) // Parse the JSON from the response
  .then(departments => {
    departments.forEach(department => {
      // Use the 'value' for the option value and 'display' for what's displayed
      $("#department").append(`<option value="${department.value}">${department.display}</option>`);
    });
  })
  .catch(err => {
    console.warn("Something went wrong.", err);
  });


  $("#tasks").on("change", function () {
    table.ajax.reload(); // Reload the DataTable
  });

  function resetFilters() {
    // Reset select elements to their default option (usually the first one)
    $("#department").val("");
    $("#theme").val("");
    // Clear text input fields
    taskSelect.setData([]);
    taskSelect.setSelected([]);
    $("#tasks").val("");

    // Reset the Date Range Picker to the initial dates
    $("#dateRangePicker").data("daterangepicker").setStartDate(moment(earliestDate));
    $("#dateRangePicker").data("daterangepicker").setEndDate(moment(latestDate));
    $("#dateRangePicker").val(earliestDate + " - " + latestDate); // Update the display

    $("#commentsCheckbox").prop("checked", false);

    // Reload the DataTable to reflect the reset filters
    table.ajax.reload();
  }
  $(".reset-filters").on("click", resetFilters);

  $("#dateRangePicker").daterangepicker(
    {
      opens: "left",
      startDate: moment(earliestDate),
      endDate: moment(latestDate),
      minDate: moment(earliestDate), // Set the earliest selectable date
      maxDate: moment(latestDate),
      alwaysShowCalendars: true,
      locale: {
        format: "YYYY/MM/DD",
        cancelLabel: isFrench ? "Effacer" : "Clear",
        applyLabel: isFrench ? "Appliquer" : "Apply",
        customRangeLabel: isFrench ? "Période spécifique" : "Custom Range",
        firstDay: isFrench ? 1 : 0, // Start with Monday
        daysOfWeek: isFrench ? ["Dim", "Lun", "Mar", "Mer", "Jeu", "Ven", "Sam"] : undefined, // Define days for French
        monthNames: isFrench ? ["Janvier", "Février", "Mars", "Avril", "Mai", "Juin", "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"] : undefined, // Define months for French
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
      // Reload the DataTable with the new date range from the input value.
      table.ajax.reload();
    }
  );

  $("#dateRangePicker").on("cancel.daterangepicker", function (ev, picker) {
    // Set the date range picker to the earliest and latest dates
    picker.setStartDate(moment(earliestDate));
    picker.setEndDate(moment(latestDate));
    // Update the input field to show the earliest and latest dates
    $("#dateRangePicker").val(moment(earliestDate).format("YYYY/MM/DD") + " - " + moment(latestDate).format("YYYY/MM/DD"));
    // Reload DataTables to reflect the reset date range
    table.ajax.reload();
  });

  $("#downloadCSV").on("click", function () {
    table.button(".buttons-csv").trigger();
  });

  $("#downloadExcel").on("click", function () {
    table.button(".buttons-excel").trigger();
  });

  tippy("#theme-tool-tip", {
    content: isFrench ? "Thèmes de navigation de Canada.ca " : "Canada.ca navigation themes ",
  });
  $("#department, #theme, #commentsCheckbox, #group").on("change", function () {
    table.ajax.reload();
  });

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
          // Debounce logic inside the Promise
          clearTimeout(taskSelect.debounceTimer); // Clear existing timer
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
          }, 800); // 300ms debounce time
        });
      },
    },
  });
});
