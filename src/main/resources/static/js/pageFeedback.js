$(document).ready(function () {
  var pageSelect = new SlimSelect({
    select: "#pages",
    settings: {
      hideSelected: true,
      keepOrder: true,
      placeholderText: "Filter by full or partial page title",
      closeOnSelect: false,
    },
    events: {
      search: (search, currentData) => {
        return new Promise((resolve, reject) => {
          // Debounce logic inside the Promise
          clearTimeout(pageSelect.debounceTimer); // Clear existing timer
          pageSelect.debounceTimer = setTimeout(() => {
            if (search.length < 2) {
              return reject("Search must be at least 2 characters");
            }

            fetch("/pageTitles?search=" + encodeURIComponent(search), {
              method: "GET",
              headers: {
                Accept: "application/json",
              },
            })
              .then((response) => {
                if (!response.ok) {
                  throw new Error("Network response was not ok");
                }
                return response.json();
              })
              .then((data) => {
                const options = data
                  .filter((title) => {
                    return !currentData.some(
                      (optionData) => optionData.value === title
                    );
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
  $("#pages").on("change", function () {
    // This function gets called when a selection is made
    table.ajax.reload(); // Reload the DataTable
  });
  var table = $("#myTable").DataTable({
    stripeClasses: [],
    "bSortClasses": false,
    order: [[0, "desc"]],
    processing: true,
    serverSide: true,
    retrieve: true,
    lengthMenu: [ [10, 25, 50, 100], [10, 25, 50, 100] ],
    orderCellsTop: true,
    fixedHeader: true,
    responsive: true,
    dom: 'Br<"table-responsive"t>tilp',
    ajax: {
      url: "/feedbackData",
      type: "GET",
      data: function (d) {
        d.titles = $("#pages").val();
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
        alert("Error retrieving data. Please refresh the page and try again.");
        console.log("xhr: " + xhr);
        console.log("error: " + error);
        console.log("thrown : " + thrown);
      },
    },
    buttons: [
      {
        extend: "csvHtml5",
        text: "Download CSV",
        className: "btn btn-default",
        exportOptions: {
          columns: [0, 4, 3, 2, 5, 6, 1, 7, 8, 9, 10], // This will export only visible columns
          modifier: {
            page: "all", // This tells DataTables to export data from all pages, not just the current page
          },
        },
        action: newexportaction,
        filename: "Page_feedback-" + new Date().toLocaleDateString(),
      },
      {
        extend: "excelHtml5",
        text: "Download Excel",
        className: "btn btn-default",
        exportOptions: {
          columns: [0, 4, 3, 2, 5, 6, 1, 7, 8, 9, 10], // This will export only visible columns
          modifier: {
            page: "all", // This tells DataTables to export data from all pages, not just the current page
          },
        },
        action: newexportaction,
        filename: "Page_feedback-" + new Date().toLocaleDateString(),
      },
    ],
    columns: [
      { data: "problemDate", width:'6%' }, // Date (visible in table)
      { data: "problemDetails", width:'50%' }, // Comments (visible in table)
      { data: "institution",width: '6%'}, // Dept (visible in table)
      { data: "title", width: '14%' }, // Page title (visible in table)
      {
        data: "url", width: '24%',
        render: function (data, type, row) {
          // Wrap any content of the 'url' column with an anchor tag
          return '<a href="' + data + '" target="_blank">' + data + "</a>";
        },
      }, // URL (visible in table)
      { data: "timeStamp", visible: false }, // Time (hidden in table, but in CSV)
      { data: "language", visible: false }, // Language (hidden in table, but in CSV)
      { data: "section", visible: false }, // Section (hidden in table, but in CSV)
      { data: "theme", visible: false }, // Theme (hidden in table, but in CSV)
      { data: "deviceType", visible: false }, // Device (hidden in table, but in CSV)
      { data: "browser", visible: false }, // Browser (hidden in table, but in CSV)
    ],
  });
  function debounce(func, delay) {
    let debounceTimer;
    return function () {
      const context = this;
      const args = arguments;
      clearTimeout(debounceTimer);
      debounceTimer = setTimeout(() => func.apply(context, args), delay);
    };
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
    pageSelect.setData([]);
    pageSelect.setSelected([]);
    $("#pages").val("");

    // Reset the Date Range Picker to the initial dates
    $("#dateRangePicker")
      .data("daterangepicker")
      .setStartDate(moment(earliestDate));
    $("#dateRangePicker")
      .data("daterangepicker")
      .setEndDate(moment(latestDate));
    $("#dateRangePicker").val(earliestDate + " - " + latestDate); // Update the display

    // Reload the DataTable to reflect the reset filters
    table.ajax.reload();
  }

  function newexportaction(e, dt, button, config) {
    var self = this;
    var oldStart = dt.settings()[0]._iDisplayStart;
    dt.one('preXhr', function (e, s, data) {
        // Just this once, load all data from the server...
        data.start = 0;
        data.length = 2147483647;
        dt.one('preDraw', function (e, settings) {
            if (button[0].className.indexOf('buttons-excel') >= 0) {
                $.fn.dataTable.ext.buttons.excelHtml5.available(dt, config) ? $.fn.dataTable.ext.buttons.excelHtml5.action.call(self, e, dt, button, config) : $.fn.dataTable.ext.buttons.excelFlash.action.call(self, e, dt, button, config);
            } else if (button[0].className.indexOf('buttons-csv') >= 0) {
                $.fn.dataTable.ext.buttons.csvHtml5.available(dt, config) ? $.fn.dataTable.ext.buttons.csvHtml5.action.call(self, e, dt, button, config) : $.fn.dataTable.ext.buttons.csvFlash.action.call(self, e, dt, button, config);
            }
            dt.one('preXhr', function (e, s, data) {
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
};

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
        format: "YYYY-MM-DD",
        cancelLabel: "Clear",
        applyLabel: "Apply",
      },
      ranges: {
        "All Dates": [moment(earliestDate), moment(latestDate)],
        Today: [moment(), moment()],
        Yesterday: [moment().subtract(1, "days"), moment().subtract(1, "days")],
        "Last 7 Days": [moment().subtract(6, "days"), moment()],
        "Last 30 Days": [moment().subtract(29, "days"), moment()],
        "This Month": [moment().startOf("month"), moment().endOf("month")],
        "Last Month": [
          moment().subtract(1, "month").startOf("month"),
          moment().subtract(1, "month").endOf("month"),
        ],
           "Last Quarter": getLastFiscalQuarter(),
      },
    },
    function (start, end, label) {
      $("#dateRangePicker").val(
        start.format("YYYY-MM-DD") + " - " + end.format("YYYY-MM-DD")
      );

      // Reload the DataTable with the new date range from the input value.
      table.ajax.reload();
    }
  );
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
  $("#dateRangePicker").on("cancel.daterangepicker", function (ev, picker) {
    // Set the date range picker to the earliest and latest dates
    picker.setStartDate(moment(earliestDate));
    picker.setEndDate(moment(latestDate));

    // Update the input field to show the earliest and latest dates
    $("#dateRangePicker").val(
      moment(earliestDate).format("YYYY-MM-DD") +
        " - " +
        moment(latestDate).format("YYYY-MM-DD")
    );

    // Reload DataTables to reflect the reset date range
    table.ajax.reload();
  });
$('#downloadCSV').on('click', function() { // Removed the 'e' parameter
    table.button('.buttons-csv').trigger();
});

$('#downloadExcel').on('click', function() { // Removed the 'e' parameter
    table.button('.buttons-excel').trigger();
});
$(document).on('click', "a[href*='design.canada.ca']", function (e) {
    // Prevent the default action
    e.preventDefault();

    // Open the link in a new tab
    window.open($(this).attr('href'), '_blank');
});
 var detailsElement = $('#filterDetails'); // Assuming you have an ID for the <details> tag
  var summaryElement = $('#filterSummary'); // Assuming you have an ID for the <summary> tag

  detailsElement.on('toggle', function() {
    if (detailsElement.prop('open')) {
      summaryElement.text('See less filters');
    } else {
      summaryElement.text('See more filters');
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
