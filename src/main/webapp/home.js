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

            // svuotare la tabella
            this.listcontainer.innerHTML = "";

            var self = this;
            var container = self.listcontainer;

            printCategory(container,arrayCategories);
            // arrayCategories.forEach((category) => printCategory(ul,category));

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




    function CreationForm(alert) {


    }




}