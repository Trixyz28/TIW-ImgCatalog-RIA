
(function() {

    // Componenti della pagina
    let pageOrchestrator = new PageOrchestrator();
    let alertContainer;
    let categoriesList;
    let creationForm;
    let startUl, startElement;
    let confirmButton;
    let modifiedList;


    window.addEventListener("load", () => {

        // controllo se l'utente è loggato
        if(sessionStorage.getItem("userinfo") == null) {
            window.location.href = "index.html";
        } else {

            // inizializza i componenti
            pageOrchestrator.start();
            pageOrchestrator.refresh();
        }
    }, false);


    function PageOrchestrator() {

        this.start = function() {

            // riferimento allo spazio per le notifiche
            alertContainer = document.getElementById("id_alert")

            // inizializzazione del saluto personalizzato
            personalMessage = new PersonalMessage(
                sessionStorage.getItem("userinfo"),
                document.getElementById("id_userinfo"));
            personalMessage.show();

            // inizializzazione della lista delle categorie
            categoriesList = new CategoriesList(
                alertContainer,
                document.getElementById("id_listcontainer")
            );


            // inizializzazione lista movimenti
            modifiedList = new Array();

            // inizializzazione del pulsante di conferma
            confirmButton = new ConfirmButton(
                alertContainer,
                document.getElementById("id_confirmbutton")
            );
            confirmButton.reset();

            document.getElementById("id_confirmbutton").addEventListener("click", (e) => {

                var self = this;

                var json = JSON.stringify(modifiedList);


                if(modifiedList.length>0) {

                    makeCallJson("POST", "MoveCategory", json,
                            function (x) {

                        if(x.readyState == XMLHttpRequest.DONE) {

                            var message = x.responseText;
                            switch (x.status) {

                                case 200:
                                    pageOrchestrator.refresh();
                                    alertContainer.textContent = message;
                                    alert("Operation completed!");
                                    break;

                                case 400: // bad request
                                    alertContainer.textContent = message;
                                    break;

                                case 401: // unauthorized
                                    alertContainer.textContent = message;
                                    break;

                                case 500: // server error
                                     alertContainer.textContent = message;
                                     break;
                                }
                            }

                        });
                } else {
                    alertContainer.textContent = "Invalid operation!"
                }
            });


            // inizializzazione del form
            creationForm = new CreationForm(
                alertContainer,
                document.getElementById("id_categoryform")
            );

            document.getElementById("id_formbutton").addEventListener(
                "click", (e) => {
                    var form  = e.target.closest("form");
                    var self = this;

                    if(form.checkValidity()) {
                        makeCall("POST", "CreateCategory", e.target.closest("form"),
                            function (x) {
                                if(x.readyState == XMLHttpRequest.DONE) {
                                    var message = x.responseText;

                                    switch (x.status) {
                                        case 200:
                                            pageOrchestrator.refresh();
                                            alertContainer.textContent = message;
                                            break;

                                        case 400: // bad request
                                            alertContainer.textContent = message;
                                            break;

                                        case 401: // unauthorized
                                            alertContainer.textContent = message;
                                            break;

                                        case 500: // server error
                                            alertContainer.textContent = message;
                                            break;
                                    }
                                }

                            });
                    } else {
                        form.reportValidity();
                    }
                }
            );


            // gestione logout
            document.getElementById("id_logout").addEventListener(
                "click", () => {
                    window.sessionStorage.removeItem("userinfo");
                }
            )

        };


        // refresh
        this.refresh = function() {
            alertContainer.textContent = "";
            categoriesList.reset();
            categoriesList.show();
            creationForm.reset();
            creationForm.show();
            confirmButton.reset();
            modifiedList = new Array();

        };

    }



    function PersonalMessage(_userinfo, messagecontainer) {
        this.userinfo = _userinfo;
        this.show = function () {
            messagecontainer.textContent = this.userinfo;
        }
    }


    function CategoriesList(_alert, listcontainer) {
        this.alert = _alert;
        this.listcontainer = listcontainer;

        this.reset = function() {
            // this.listcontainer.style.visibility = "hidden";
        }

        this.show = function() {

            // per la chiusura
            var self = this;

            makeCall("GET", "GetCategoryListData", null, function(req) {

                if(req.readyState == 4) {
                    var message = req.responseText;

                    if(req.status == 200) {

                        var categoriesToShow = JSON.parse(message);

                        if(categoriesToShow.length == 0) {
                            self.alert.textContent = "There are no categories present!";
                            return;
                        }
                        self.update(categoriesToShow);

                    } else if(req.status == 403) {
                        window.location.href = req.getResponseHeader("Location");
                        window.sessionStorage.removeItem("userinfo");

                    } else {
                        self.alert.textContent = message;
                    }
                }
            });
        };


        this.update = function(arrayCategories) {

            // svuotare la tabella
            this.listcontainer.innerHTML = "";

            var self = this;
            var container = self.listcontainer;

            printCategory(container,arrayCategories);

            this.listcontainer.style.visibility = "visible";
        }
    }

    function ConfirmButton(_alert, button) {

        this.reset = function() {
            button.style.visibility = "hidden";
            button.disabled = true;
        }

        this.show = function () {
            button.disabled = false;
            button.style.visibility = "visible";
        }

    }


    function printCategory(container, categories) {

        if(!categories) {
            return;
        }

        const ul = document.createElement("ul");

        for(category of categories) {

            var categoryNum, categoryName, linkText, anchor;
            var label;

            const li = document.createElement("li");
            li.draggable = true;

            categoryNum = document.createTextNode( category.position + " ");
            categoryName = document.createTextNode(category.name + "  ");

            label = document.createElement("label");
            label.appendChild(categoryNum)
            label.appendChild(categoryName);
            label.setAttribute("categoryid", category.id);
            li.appendChild(label);

            li.setAttribute("categoryid",category.id);

            setListEvents(li,label);

            if(category.subClasses) {
                printCategory(li,category.subClasses);
            }
            ul.appendChild(li);

        }
        container.appendChild(ul);
    }

    function setListEvents(li,label) {

        li.addEventListener("dragstart",(e) => {
            dragStart(e);
        });

        li.addEventListener("dragover", (e) => {
            dragOver(e);
        });

        li.addEventListener("dragleave", (e) => {
            dragLeave(e);
        });

        label.addEventListener("drop", (e) => {
           drop(e);
        });

    }




    function CreationForm(_alert, formcontainer) {

        this.alert = _alert;

        this.reset = function () {
            document.getElementById("id_formdiv").style.display = "none";
        }

        this.show = function() {

            var self = this;

            document.getElementById("id_formdiv").style.display = "block";

            makeCall("GET", "GetCategoryFormData", null, function(req) {

                if(req.readyState == 4) {
                    var message = req.responseText;

                    if(req.status == 200) {

                        var optionsToShow = JSON.parse(message);

                        if(optionsToShow.length == 0) {
                            self.alert.textContent = "There are no categories present!";
                            return;
                        }
                        self.update(optionsToShow);

                    } else if(req.status == 403) {
                        window.location.href = req.getResponseHeader("Location");
                        window.sessionStorage.removeItem("userinfo");

                    } else {
                        self.alert.textContent = message;
                    }
                }
            });
        }

        this.update = function(arrayOptions) {

            var selection = document.getElementById("id_selection");
            selection.innerHTML = "";

            arrayOptions.forEach((element) => {
                opt = document.createElement("option");
                opt.value = element.id;
                opt.text = element.name;
                selection.appendChild(opt);
            })

        }


    }


    function unselectRows(rowsArray) {
        for (var i = 0; i < rowsArray.length; i++) {
            rowsArray[i].className = "notselected";
        }
    }

    /* The dragstart event is fired when the user starts dragging an element (if it is draggable=True) */
    function dragStart(event) {
        /* we need to save in a variable the row that provoked the event
         to then move it to the new position */
        startUl = event.target.closest("ul");
        startElement = event.target.closest("li");
        creationForm.reset();
    }


    /* The dragover event is fired when an element is being dragged over a valid drop target. */
    function dragOver(event) {
        // We need to use prevent default, otherwise the drop event is not called
        event.preventDefault();

        var dest = event.target.closest("li");

        // Mark  the current element as "selected", then with CSS we will put it in red
        dest.className = "selected";
    }


    /* The dragleave event is fired when a dragged element leaves a valid drop target. */
    function dragLeave(event) {
        // We need to select the row that triggered this event to marked as "notselected" so it's clear for the user
        var dest = event.target.closest("li");

        // Mark  the current element as "notselected", then with CSS we will put it in black
        dest.className = "notselected";
    }

    /* The drop event is fired when an element or text selection is dropped on a valid drop target. */
    function drop(event) {

        var destLi = event.target.closest("li");
        var destUl = destLi.lastChild;


        if(!startElement.contains(destUl)) {
            destUl.appendChild(startElement);
            destLi.className = "notselected";
            startElement.className = "notselected";

            var fid = destLi.getAttribute("categoryid");
            var cid = startElement.getAttribute("categoryid");


            if(confirm("Do you want to confirm the move?")) {
                modifiedList.push([fid,cid]);
                // console.table(modifiedList);
                confirmButton.show();
            } else {
                startUl.appendChild(startElement);
            }
        }

    }


})();


