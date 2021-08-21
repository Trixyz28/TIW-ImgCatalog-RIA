(function() {

    // Variable to save the reference of the dragged element
    let startElement;
    let modifiedList;







    /*
        This function puts all row to "notselected" class,
        then we use CSS to put "notselected" in black and "selected" in red
    */
    function unselectRows(rowsArray) {
        for (var i = 0; i < rowsArray.length; i++) {
            rowsArray[i].className = "notselected";
        }
    }


    /*
        The dragstart event is fired when the user starts
        dragging an element (if it is draggable=True)
        https://developer.mozilla.org/en-US/docs/Web/API/Document/dragstart_event
    */


    /*
        The dragover event is fired when an element
        is being dragged over a valid drop target.
        https://developer.mozilla.org/es/docs/Web/API/Document/dragover_event
    */


    /*
        The dragleave event is fired when a dragged
        element leaves a valid drop target.
        https://developer.mozilla.org/en-US/docs/Web/API/Document/dragleave_event
    */


    /*
        The drop event is fired when an element or text selection is dropped on a valid drop target.
        https://developer.mozilla.org/en-US/docs/Web/API/Document/drop_event
    */







})();


