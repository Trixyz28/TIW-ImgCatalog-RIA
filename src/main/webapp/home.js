{

    // Componenti della pagina
    let pageOrchestrator = new PageOrchestrator();
    let categoriesList;


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
                document.getElementById("id_listcontainer"),
                document.getElementById("id_listcontainerbody")
            );


            // inizializzazione del form



            // gestione logout
            document.getElementById("id_logout").addEventListener(
                "click", () => {
                    window.sessionStorage.removeItem("userinfo");
                }
            )

        };

        // refresh con

        this.refresh = function() {
            alertContainer.textContent = "";
            categoriesList.reset();
            categoriesList.show();

        };

    }



    function PersonalMessage(_userinfo, messagecontainer) {
        this.userinfo = _userinfo;
        this.show = function () {
            messagecontainer.textContent = this.userinfo;
        }
    }


    function CategoriesList(_alert, listcontainer, listcontainerbody) {
        this.alert = _alert;
        this.listcontainer = listcontainer;
        this.listcontainerbody = listcontainerbody;

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

            var row, span, categoryNum, categoryName, link, linkText, anchor;

            // svuotare la tabella
            this.listcontainerbody.innerHTML = "";

            var self = this;

            arrayCategories.forEach(function printCategory(category) {


                row = document.createElement("li");

                categoryNum = document.createTextNode(category.position + " ");
                categoryName = document.createTextNode(category.name + " ");

                row.appendChild(categoryNum);
                row.appendChild(categoryName);

                anchor = document.createElement("a");
                row.appendChild(anchor);

                linkText = document.createTextNode("Sposta");
                anchor.appendChild(linkText);
                anchor.setAttribute("categoryid",category.id);
                anchor.addEventListener("click", (e) => {
                    row.visibility = "hidden";
                }, false);

                anchor.href = "#";
                self.listcontainerbody.appendChild(row);


            });

            this.listcontainer.style.visibility = "visible";
        }
    }

    function printCategory() {

    }




    function CreationForm(alert) {


    }




}