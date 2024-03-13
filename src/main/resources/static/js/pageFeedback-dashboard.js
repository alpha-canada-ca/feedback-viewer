$(document).ready(function () {
  // Function to parse the query string and get the value of a specific parameter
  function getQueryParam(param) {
    var searchParams = new URLSearchParams(window.location.search);
    return searchParams.get(param);
  }

  // Check if the 'lang' query parameter is set to 'fr'
  var isFrench = langSession === "fr";
  var now = new Date();
  var formattedDate = now.getMonth() + 1 + "/" + now.getDate() + "/" + now.getFullYear();
    var formattedEarliestDate = moment(earliestDate).format("YYYY/MM/DD");
    var formattedLatestDate = moment(latestDate).format("YYYY/MM/DD");
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

  function resetFilters() {
    // Reset select elements to their default option (usually the first one)
    $("#department").val("");
    $("#language").val("");
    $("#theme").val("");
    $("#section").val("");
    // Clear text input fields
    $("#url").val("");
    $("#comments").val("");

    // Reset the Date Range Picker to the initial dates
    // Format the earliest and latest dates in YYYY/MM/DD format


    // Reset the Date Range Picker to the initial dates with formatted strings
    $("#dateRangePicker").data("daterangepicker").setStartDate(formattedEarliestDate);
    $("#dateRangePicker").data("daterangepicker").setEndDate(formattedLatestDate);

    // Update the display with formatted dates
    $("#dateRangePicker").val(formattedEarliestDate + " - " + formattedLatestDate);

    // Reload the DataTable to reflect the reset filters
    table.ajax.reload();
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

  // DataTable initialization
  var table = $("#myTable").DataTable({
    language: isFrench ? { url: "//cdn.datatables.net/plug-ins/1.10.21/i18n/French.json" } : undefined,
    stripeClasses: [],
    bSortClasses: false,
    order: [[0, "desc"]],
    processing: true,
    serverSide: true,
    retrieve: true,
    lengthMenu: [
      [10, 25, 50, 100],
      [10, 25, 50, 100],
    ],
    orderCellsTop: true,
    fixedHeader: true,
    responsive: true,
    drawCallback: function() {
        fetchTotalCommentsCount();
        fetchTotalPagesCount();
          fetchDataAndCreateChart();
    },
    dom: 'Br<"table-responsive"t>tilp',
    ajax: {
      url: "/dashboardData",
      type: "GET",
      data: function (d) {
        d.language = $("#language").val();
        d.department = $("#department").val();
        d.comments = $("#comments").val();
        d.section = $("#section").val();
        d.theme = $("#theme").val();
        d.url = $("#url").val();
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
      },
      error: function (xhr, error, thrown) {
        alert(isFrench ? "Erreur lors de la récupération des données. Veuillez rafraîchir la page et réessayer." : "Error retrieving data. Please refresh the page and try again.");
        console.log("xhr: " + xhr);
        console.log("error: " + error);
        console.log("thrown : " + thrown);
      },
    },
    buttons: [
      {
        extend: "csvHtml5",
        className: "btn btn-default",
        exportOptions: {
          columns: [2,1,3,0,4,5], // This will export only visible columns
          modifier: {
            page: "all", // This tells DataTables to export data from all pages, not just the current page
          },
        },
        action: newexportaction,
        filename: (isFrench ? "Outil_de_retroaction-" : "Page_feedback-") + new Date().getFullYear() + "-" + ("0" + (new Date().getMonth() + 1)).slice(-2) + "-" + ("0" + new Date().getDate()).slice(-2),
      },
      {
        extend: "excelHtml5",
        className: "btn btn-default",
        exportOptions: {
          columns: [2,1,3,0,4,5], // This will export only visible columns
          modifier: {
            page: "all", // This tells DataTables to export data from all pages, not just the current page
          },
        },
        action: newexportaction,
        filename: (isFrench ? "Outil_de_retroaction-" : "Page_feedback-") + new Date().getFullYear() + "-" + ("0" + (new Date().getMonth() + 1)).slice(-2) + "-" + ("0" + new Date().getDate()).slice(-2),
      },
    ],
    columns: [
      { data: "institution" }, // Dept (visible in table)
       { data: "url",      render: function (data, type, row) {
                               // Wrap any content of the 'url' column with an anchor tag
                               return '<a href="' + data + '" target="_blank">' + data + "</a>";
                             },},
      { data: "urlEntries" },
      { data: "language", visible: false }, // Language (hidden in table, but in CSV)
      { data: "section", visible: false }, // Section (hidden in table, but in CSV)
      { data: "theme", visible: false }, // Theme (hidden in table, but in CSV)
    ],
  });
function fetchTotalCommentsCount() {
    fetch('/pageFeedback/totalCommentsCount')
        .then(response => response.text())
        .then(totalCommentsCount => {
            // Update the total comments count in the <span class="number"> element
            $('.stat .totalCommentCount').text(totalCommentsCount);
        })
        .catch(err => {
            console.warn('Something went wrong.', err);
        });
}

function fetchTotalPagesCount() {
    fetch('/pageFeedback/totalPagesCount')
        .then(response => response.text())
        .then(totalPagesCount => {
            // Update the total comments count in the <span class="number"> element
            $('.stat .totalPagesCount').text(totalPagesCount);
        })
        .catch(err => {
            console.warn('Something went wrong.', err);
        });
}

  $(".reset-filters").on("click", function () {
    resetFilters();
  });

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
    $("#dateRangePicker").val(formattedEarliestDate + " - " + formattedLatestDate);
    // Reload DataTables to reflect the reset date range
    table.ajax.reload();
  });

  $("#downloadCSV").on("click", function () {
    table.button(".buttons-csv").trigger();
  });

  $("#downloadExcel").on("click", function () {
    table.button(".buttons-excel").trigger();
  });

  //  $(document).on("click", "a[href*='design.canada.ca'], a[href*='conception.canada.ca']", function (e) {
  //    e.preventDefault(); // Prevent the default link behavior
  //    window.open($(this).attr("href"), "_blank"); // Open the link in a new tab/window
  //  });

  tippy("#section-tool-tip", {
    content: isFrench ? "Une valeur ajoutée manuellement à certaines pages" : "A value manually added to select pages",
  });

  tippy("#theme-tool-tip", {
    content: isFrench ? "Thèmes de navigation de Canada.ca " : "Canada.ca navigation themes ",
  });

  // Define the function that will fetch data and create the chart
  function fetchDataAndCreateChart() {
    // Replace '/path/to/your/endpoint' with the actual path to your backend endpoint
    fetch('/chartData')
      .then(response => response.json())
      .then(data => {
        // Parse the data to get the categories and the series data
        const categories = data.map(item => item.date);
        const commentsData = data.map(item => item.comments);

        // Now create the chart with the data
        Highcharts.chart('chart', {
            chart: {
                type: 'column'
            },
            title: {
                text: 'Daily Comments',
                align: 'left',
                  style: {
                            fontSize: '20px' // Adjust title font size here
                        }
            },
            xAxis: {
                categories: categories, // Set the categories from the data
                crosshair: true,
                accessibility: {
                    description: 'Dates'
                },
                labels: {
                            style: {
                                fontSize: '14px' // Adjust X axis labels font size here
                            }
                        }
            },
            yAxis: {
                min: 0,
                title: {
                    text: 'Number of Comments',
                     style: {
                                    fontSize: '20px', // Adjust Y axis title font size here
                                    fontWeight: 'bold'
                                }
                },
                 labels: {
                            style: {
                                fontSize: '17px' // Adjust Y axis labels font size here
                            }
                        }
            },
            tooltip: {
                valueSuffix: ' comments',
                 style: {
                            fontSize: '16px' // Adjust font size for text in the tooltip on hover
                        }
            },
            plotOptions: {
                column: {
                        pointPadding: 0, // Minimizes the space between points within the same category
                                groupPadding: 0.1, // Adjust space between categories
                                borderWidth: 0
                }
            },
            series: [
                {
                    name: 'Comments',
                    data: commentsData // Set the data from the data
                }
            ]
        });
      })
      .catch(error => {
        console.error('Error fetching data: ', error);
      });
  }

  // Call the function to fetch data and create the chart



  var detailsElement = $("#filterDetails");
  var summaryElement = $("#filterSummary");

  detailsElement.on("toggle", function () {
    if (detailsElement.prop("open")) {
      summaryElement.text(isFrench ? "Voir moins de filtres" : "See less filters");
    } else {
      summaryElement.text(isFrench ? "Voir plus de filtres" : "See more filters");
    }
  });

  $("#language, #department, #section, #theme").on("change", function () {
    table.ajax.reload();
  });

  $("#comments, #url").on(
    "keyup",
    debounce(function (e) {
      table.ajax.reload(); // Reload the table without resetting pagination
    }, 800)
  );
});
