{

    // Componenti della pagina
    let pageOrchestrator = new PageOrchestrator();
    let categoriesList;
    let creationForm;


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

        // riferimento allo spazio per le notifiche
        var alertContainer = document.getElementById("id_alert")

        this.start = function() {

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
                                            self.refresh();
                                            self.alert.textContent = message;
                                            break;

                                        case 400: // bad request
                                            self.alert.textContent = message;
                                            break;

                                        case 401: // unauthorized
                                            self.alert.textContent = message;
                                            break;

                                        case 500: // server error
                                            self.alert.textContent = message;
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


    function printCategory(container, categories) {
        if(!categories) {
            return;
        }

        const ul = document.createElement("ul");

        for(category of categories) {

            var categoryNum, categoryName, linkText, anchor;

            const li = document.createElement("li");
            categoryNum = document.createTextNode( category.position + " ");
            categoryName = document.createTextNode(category.name + "  ");

            li.appendChild(categoryNum);
            li.appendChild(categoryName);

            li.setAttribute("categoryid",category.id);


            if(category.subClasses) {
                printCategory(li,category.subClasses);
            }
            ul.appendChild(li);

        }
        container.appendChild(ul);
    }




    function CreationForm(_alert, formcontainer) {

        this.alert = _alert;

        this.reset = function () {

        }

        this.show = function() {

            var self = this;
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




}