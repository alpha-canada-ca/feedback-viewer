
$(document).ready(function () {
    var pageSelect = new SlimSelect({
        select: '#pages',
        settings: {
            hideSelected: true,
            keepOrder: true,
            placeholderText: 'Custom Placeholder Text',
            closeOnSelect: false,
        },
        events: {
            search: (search, currentData) => {
                return new Promise((resolve, reject) => {
                    // Debounce logic inside the Promise
                    clearTimeout(pageSelect.debounceTimer); // Clear existing timer
                    pageSelect.debounceTimer = setTimeout(() => {
                        if (search.length < 2) {
                            return reject('Search must be at least 2 characters');
                        }
    
                        fetch('/pageTitles?search=' + encodeURIComponent(search), {
                            method: 'GET',
                            headers: {
                                'Accept': 'application/json',
                            }
                        })
                        .then(response => {
                            if (!response.ok) {
                                throw new Error('Network response was not ok');
                            }
                            return response.json();
                        })
                        .then(data => {
                            const options = data
                                .filter(title => {
                                    return !currentData.some(optionData => optionData.value === title);
                                })
                                .map(title => {
                                    return { text: title, value: title };
                                });
    
                            resolve(options);
                        })
                        .catch(error => {
                            console.error('Error fetching page titles:', error);
                            reject(error);
                        });
                    }, 800); // 300ms debounce time
                });
            }
        }
        
    }); 
    $('#pages').on('change', function () {
        // This function gets called when a selection is made
        table.ajax.reload(); // Reload the DataTable
    });
    var table = $('#myTable').DataTable({
        "order": [
            [0, "desc"]
        ],
        processing: true,
        serverSide: true,
        retrieve: true,
        lengthMenu: false,
        orderCellsTop: true,
        fixedHeader: true,
        dom: 'Br<"table-responsive"t>tip',
        ajax: {
            url: '/feedbackData',
            type: 'GET',
            data: function (d) { 
                d.titles = $('#pages').val();
                d.language = $('#language').val();
                d.department = $('#department').val();
                d.comments = $('#comments').val();
                d.url = $('#url').val(); 
                var dateRangePickerValue = $('#dateRangePicker').val();
                if (dateRangePickerValue) {
                    var dateRange = $('#dateRangePicker').data('daterangepicker');
                    d.startDate = dateRange.startDate.format('YYYY-MM-DD');
                    d.endDate = dateRange.endDate.format('YYYY-MM-DD');
                } else {
                    // If the date range picker is empty, do not send startDate and endDate in the request
                    delete d.startDate; // Ensure startDate is not included in the AJAX request
                    delete d.endDate;   // Ensure endDate is not included in the AJAX request
                }
            },
            error: function (xhr, error, thrown) {
                alert("Error retrieving data. Please refresh the page and try again.");
                console.log("xhr: " + xhr);
                console.log("error: " + error);
                console.log("thrown : " + thrown);
            }
        },
        buttons: [
            {
                extend: 'csvHtml5',
                text: 'Download CSV',
                className: 'btn btn-default',
                exportOptions: {
                    columns: [0, 4, 3, 2, 5, 6, 1, 7, 8, 9, 10],  // This will export only visible columns
                    modifier: {
                        page: 'all' // This tells DataTables to export data from all pages, not just the current page
                    }
                }, "action": newexportaction,
                filename: 'data_export_' + new Date().toLocaleDateString()
            },
            {
                extend: 'excelHtml5',
                text: 'Download Excel',
                className: 'btn btn-default',
                exportOptions: {
                    columns: [0, 4, 3, 2, 5, 6, 1, 7, 8, 9, 10],  // This will export only visible columns
                    modifier: {
                        page: 'all' // This tells DataTables to export data from all pages, not just the current page
                    }
                }, "action": newexportaction,
                filename: 'data_export_' + new Date().toLocaleDateString()
            }
        ],
        columns: [
            { data: 'problemDate' }, // Date (visible in table)
            { data: 'problemDetails' }, // Comments (visible in table)
            { data: 'institution' }, // Dept (visible in table)
            { data: 'title' }, // Page title (visible in table)
            {
                data: 'url',
                render: function (data, type, row) {
                    // Wrap any content of the 'url' column with an anchor tag
                    return '<a href="' + data + '" target="_blank">' + data + '</a>';
                }
            }, // URL (visible in table)
            { data: 'timeStamp', visible: false }, // Time (hidden in table, but in CSV)
            { data: 'language', visible: false }, // Language (hidden in table, but in CSV)
            { data: 'section', visible: false }, // Section (hidden in table, but in CSV)
            { data: 'theme', visible: false }, // Theme (hidden in table, but in CSV)
            { data: 'deviceType', visible: false }, // Device (hidden in table, but in CSV)
            { data: 'browser', visible: false } // Browser (hidden in table, but in CSV)
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
        $('#department').val('');
        $('#language').val('');

        // Clear text input fields
        $('#url').val('');
        $('#comments').val('');
        pageSelect.setData([]);
        pageSelect.setSelected([]);
        $('#pages').val('');

        // Reset the Date Range Picker to the initial dates
        $('#dateRangePicker').data('daterangepicker').setStartDate(moment(earliestDate));
        $('#dateRangePicker').data('daterangepicker').setEndDate(moment(latestDate));
        $('#dateRangePicker').val(earliestDate + ' - ' + latestDate); // Update the display

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

    $('.reset-filters').on('click', function () {
        resetFilters();
    });
    $('#dateRangePicker').daterangepicker({
        opens: 'left',
        startDate: moment(earliestDate),
        endDate: moment(latestDate),
        alwaysShowCalendars: true,
        locale: {
            format: 'YYYY-MM-DD',
            cancelLabel: 'Clear',
            applyLabel: 'Apply'
        },
        ranges: {
            'All Dates': [moment(earliestDate), moment(latestDate)],
            'Today': [moment(), moment()],
            'Yesterday': [moment().subtract(1, 'days'), moment().subtract(1, 'days')],
            'Last 7 Days': [moment().subtract(6, 'days'), moment()],
            'Last 30 Days': [moment().subtract(29, 'days'), moment()],
            'This Month': [moment().startOf('month'), moment().endOf('month')],
            'Last Month': [moment().subtract(1, 'month').startOf('month'), moment().subtract(1, 'month').endOf('month')],
        }
    }, function (start, end, label) {
        $('#dateRangePicker').val(start.format('YYYY-MM-DD') + ' - ' + end.format('YYYY-MM-DD'));

        // Reload the DataTable with the new date range from the input value.
        table.ajax.reload();
    });

    $('#dateRangePicker').on('cancel.daterangepicker', function (ev, picker) {
        // Set the date range picker to the earliest and latest dates
        picker.setStartDate(moment(earliestDate));
        picker.setEndDate(moment(latestDate));

        // Update the input field to show the earliest and latest dates
        $('#dateRangePicker').val(moment(earliestDate).format('YYYY-MM-DD') + ' - ' + moment(latestDate).format('YYYY-MM-DD'));

        // Reload DataTables to reflect the reset date range
        table.ajax.reload();
    });

    $('#language, #department').on('change', function () {
        table.ajax.reload();
    });
    $('#comments, #url').on('keyup', debounce(function (e) {
        table.ajax.reload(); // Reload the table without resetting pagination
    }, 800));

});