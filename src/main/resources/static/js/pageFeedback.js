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
        }
        dt.one("preXhr", function (e, s, data) {
          // DataTables thinks the first item displayed is index 0, but we're not drawing that.
          // Set the property to what it was before exporting.
          settings._iDisplayStart = oldStart;
          data.start = oldStart;
        });
        setTimeout(dt.ajax.reload, 0); // Prevent rendering of the full data to the DOM
        return false;
      });
    });
    dt.ajax.reload();
  }

  function resetFilters() {
    // Reset select elements to their default option (usually the first one)
    $("#department").val("");
    $("#language").val("");
    $("#errorComments").prop("checked", false);
    $("#theme").val("");
    $("#section").val("");
    // Clear text input fields
    $("#url").val("");
    $("#comments").val("");
    pageSelect.setData([]);
    pageSelect.setSelected([]);
    $("#pages").val("");

    // Reset the Date Range Picker to the initial dates
    updateDateRangePicker();
    table.ajax.reload();
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

  function initializeDateRangePicker() {
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
  }

  function updateDateRangePicker() {
    var dateRangePicker = $("#dateRangePicker").data("daterangepicker");
    dateRangePicker.setStartDate(moment(earliestDate));
    dateRangePicker.setEndDate(moment(latestDate));
    $("#dateRangePicker").val(moment(earliestDate).format("YYYY/MM/DD") + " - " + moment(latestDate).format("YYYY/MM/DD"));
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
        if ($("#errorComments").prop("checked")) {
          d.error_keyword = "true";  // Only send if checked
      }
      
        var dateRangePickerValue = $("#dateRangePicker").val();
        if (dateRangePickerValue) {
          var dateRange = $("#dateRangePicker").data("daterangepicker");
          d.startDate = dateRange.startDate.format("YYYY-MM-DD");
          d.endDate = dateRange.endDate.format("YYYY-MM-DD");
        } else {
          delete d.startDate;
          delete d.endDate;
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
        extend: 'csvHtml5',
        className: 'btn btn-default',
        text: isFrench ? 'Télécharger CSV' : 'Download CSV',
        action: function (e, dt, button, config) {
          e.preventDefault();
          var url = new URL(window.location.origin + '/exportCSV');
          var params = getFilterParams();
          Object.keys(params).forEach(key => url.searchParams.append(key, params[key]));
          window.location.href = url.toString();
        }
      },
      {
        extend: 'excelHtml5',
        className: 'btn btn-default',
        text: isFrench ? 'Télécharger Excel' : 'Download Excel',
        action: function (e, dt, button, config) {
          e.preventDefault();
          var url = new URL(window.location.origin + '/exportExcel');
          var params = getFilterParams();
          Object.keys(params).forEach(key => url.searchParams.append(key, params[key]));
          window.location.href = url.toString();
        }
      }
    ],
    columns: [
      { data: "problemDate", width: "6%" },
      { data: "problemDetails", width: "50%" },
      { data: "institution", width: "6%" },
      { data: "title", width: "14%" },
      {
        data: "url",
        width: "24%",
        render: function (data, type, row) {
          return '<a href="' + data + '" target="_blank">' + data + "</a>";
        },
      },
      { data: "timeStamp", visible: false },
      { data: "language", visible: false },
      { data: "section", visible: false },
      { data: "theme", visible: false },
      { data: "deviceType", visible: false },
      { data: "browser", visible: false },
    ],
  });

  // SlimSelect initialization
  var pageSelect = new SlimSelect({
    select: "#pages",
    settings: {
      hideSelected: true,
      keepOrder: true,
      placeholderText: isFrench ? "Filtrer par titre de page complet ou partiel" : "Filter by full or partial page title",
      searchText: isFrench ? "Aucun résultat trouvé" : "No results found",
      searchPlaceholder: isFrench ? "Recherche" : "Search",
      searchingText: isFrench ? "Recherche en cours..." : "Searching...",
      closeOnSelect: false,
    },
    events: {
      search: (search, currentData) => {
        return new Promise((resolve, reject) => {
          clearTimeout(pageSelect.debounceTimer);
          pageSelect.debounceTimer = setTimeout(() => {
            if (search.length < 2) {
              return reject(isFrench ? "La recherche doit comporter au moins 2 caractères" : "Search must be at least 2 characters");
            }

            fetch("/pageTitles?search=" + encodeURIComponent(search), {
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
                  .filter((title) => !currentData.some((optionData) => optionData.value === title))
                  .map((title) => ({ text: title, value: title }));

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

  // Event bindings
  $("#pages").on("change", function () {
    table.ajax.reload();
  });

  $(".reset-filters").on("click", resetFilters);

  initializeDateRangePicker();

  $("#downloadCSV").on("click", function () {
    var url = new URL(window.location.origin + '/exportCSV');
    var params = getFilterParams();
    Object.keys(params).forEach(key => url.searchParams.append(key, params[key]));
    window.location.href = url.toString();
  });

  $("#downloadExcel").on("click", function () {
    var url = new URL(window.location.origin + '/exportExcel');
    var params = getFilterParams();
    Object.keys(params).forEach(key => url.searchParams.append(key, params[key]));
    window.location.href = url.toString();
  });

  tippy("#section-tool-tip", {
    content: isFrench ? "Une valeur ajoutée manuellement à certaines pages" : "A value manually added to select pages",
  });

  tippy("#theme-tool-tip", {
    content: isFrench ? "Thèmes de navigation de Canada.ca " : "Canada.ca navigation themes ",
  });

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

  // Handle error comments checkbox
  $("#errorComments").on("change", function () {
    const $label = $(this).closest('label');
    if ($(this).is(':checked')) {
      $label.addClass('active');
    } else {
      $label.removeClass('active');
    }
    table.ajax.reload();
  });

  $("#comments, #url").on(
    "keyup",
    debounce(function (e) {
      table.ajax.reload();
    }, 800)
  );

  // Add this new function to get filter parameters
  function getFilterParams() {
    var params = {
      titles: $("#pages").val(),
      language: $("#language").val(),
      error_keyword: $("#errorComments").prop("checked"),
      department: $("#department").val(),
      comments: $("#comments").val(),
      section: $("#section").val(),
      theme: $("#theme").val(),
      url: $("#url").val()
    };

    var dateRangePickerValue = $("#dateRangePicker").val();
    if (dateRangePickerValue) {
      var dateRange = $("#dateRangePicker").data('daterangepicker');
      params.startDate = dateRange.startDate.format('YYYY-MM-DD');
      params.endDate = dateRange.endDate.format('YYYY-MM-DD');
    }

    return params;
  }
});
