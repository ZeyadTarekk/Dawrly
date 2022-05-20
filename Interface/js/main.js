let dawarhaLogo = document.querySelector(".dawarha-logo-results");
if (dawarhaLogo) {
    dawarhaLogo.addEventListener("click", function() {
        window.location.href = "http://localhost:8080/SearchEngine/BuildInterface?";
    });
}

if (document.title != "Dawrha Search") {
    //chose which page to show
    let currentPage = 1;

    //the div that contains all the search results
    let searchResults = document.querySelector(`.search-results`);
    let pagesNum = searchResults.childElementCount;
    //number of pages to show the results
    let counter = Math.ceil(pagesNum / 10);

    //the previous button span
    let prev = document.querySelector(`.numbers .previous-btn`);
    prev.children[1].addEventListener("click", function(e) {
        decrease(e);
    });

    //the next button span
    let next = document.querySelector(`.numbers .next-btn`);
    next.children[1].addEventListener("click", function(e) {
        increase(e);
    });

    if (counter == 1) {
        next.classList.add("hidden");
    }


    if (pagesNum == 0) {
        //add there is no results to show
        let noResults = document.createElement("div");
        noResults.className = "no-results";
        noResults.innerHTML = "<p>Oops, there is no results to show</p>";
        searchResults.appendChild(noResults);

        let numbers = document.querySelector(".numbers");
        numbers.classList.add("hidden");

    } else if (pagesNum <= 1) {
        next.classList.add("hidden");
    }



    if (pagesNum > 10) {

        // show the first 10 results only
        let start = (currentPage - 1) * 10;
        for (let i = start; i < start + 10; i++) {
            searchResults.children[i].classList.remove("hidden");
        };
    } else {
        //show all of the results (< 10)
        for (let i = 0; i < pagesNum; i++) {
            searchResults.children[i].classList.remove("hidden");
        };
    }

    if (pagesNum > 0) {
        //add numbers to move between the pages
        let numbersDiv = document.querySelector(`.pages-numbers`);
        numbersDiv.children[0].children[0].addEventListener("click", function(e) {
            changePages(1, e);
        });
        for (let i = 2; i <= counter; i++) {
            let span = document.createElement("span");
            span.classList.add("r-char");

            //show only the first 10 numbers
            if (i > 10)
                span.classList.add("hidden");

            span.innerHTML = "h <a>" + i + "</a>";
            span.children[0].addEventListener("click", function(e) {
                changePages(i, e);
            });
            numbersDiv.appendChild(span);
        };

        //hide all the numbers
        for (let i = 1; i <= numbersDiv.childElementCount; i++) {
            numbersDiv.children[i-1].classList.add("hidden");
        }
        //show the first set
        for (let i = 1; i <= 10; i++) {
            numbersDiv.children[i-1].classList.remove("hidden");
        }
    }


    function changePages(num, e) {
        //move the current page number to the new number
        let numbersDiv = document.querySelector(`.pages-numbers`);
        for (let i = 1; i <= numbersDiv.childElementCount; i++) {
            if (i != num) {
                numbersDiv.children[i-1].children[0].classList.remove("selected");
            } else {
                numbersDiv.children[i-1].children[0].classList.add("selected");
            }
        }

        let prevPage = currentPage;
        currentPage = num;

        if (prevPage != currentPage) {
            //show or hide next and previous buttons
            if (currentPage != 1) {
                prev.classList.remove("hidden");
            } else {
                prev.classList.add("hidden");
            }

            if (currentPage == counter) {
                next.classList.add("hidden");
            } else {
                next.classList.remove("hidden");
            }

            //show the current set of numbers [1->10] [11->20] ...
            if ((currentPage - 1) % 10 == 0) {
                //hide all the numbers
                for (let i = 1; i <= numbersDiv.childElementCount; i++) {
                    numbersDiv.children[i-1].classList.add("hidden");
                }
                //show the current set only
                if (currentPage + 10 >= counter) {
                    for (let i = currentPage; i <= counter; i++) {
                        numbersDiv.children[i-1].classList.remove("hidden");
                    }
                } else {
                    for (let i = currentPage; i < currentPage + 10; i++) {
                        numbersDiv.children[i-1].classList.remove("hidden");
                    }
                }
            } else if (currentPage % 10 == 0) {
                //hide all the numbers
                for (let i = 1; i <= numbersDiv.childElementCount; i++) {
                    numbersDiv.children[i-1].classList.add("hidden");
                }
                //show the current set only
                if (currentPage - 10 == 0) {
                    for (let i = currentPage; i >= 1; i--) {
                        numbersDiv.children[i-1].classList.remove("hidden");
                    }
                } else {
                    for (let i = currentPage; i > currentPage - 10; i--) {
                        numbersDiv.children[i-1].classList.remove("hidden");
                    }
                }
            }


            if (pagesNum > 10) {

                let start = (currentPage - 1) * 10;
                let end = start + 10;
                if (end > pagesNum) {
                    end = pagesNum;
                }
                for (let i = start; i < end; i++) {
                    searchResults.children[i].classList.remove("hidden");
                };

                start = (prevPage - 1) * 10;
                end = start + 10;
                if (end > pagesNum) {
                    end = pagesNum;
                }
                for (let i = start; i < end; i++) {
                    searchResults.children[i].classList.add("hidden");
                };

            } else {
                for (let i = 0; i < pagesNum; i++) {
                    searchResults.children[i].classList.remove("hidden");
                };
            }
            document.body.scrollTop = 0; // For Safari
            document.documentElement.scrollTop = 0; // For Chrome, Firefox, IE and Opera
        }
    }

    function increase(e) {
        changePages((currentPage + 1), e);
    }

    function decrease(e) {
        changePages((currentPage - 1), e);
    }
}


function getSuggestions() {
    let suggestions = document.querySelector(`.suggestions`);
    if (suggestions != null) {
        for (let i = 0; i < suggestions.childElementCount; i++) {
            suggestionsArray.push(suggestions.children[i].innerText);
        }

        //remove the suggestions div
        document.body.removeChild(suggestions);
    }
}

function autocomplete(inp, arr) {
    var currentFocus;
    inp.addEventListener("input", function(e) {
        var a, b, i, val = this.value;
        closeAllLists();

        if (!val) { return false;}
        currentFocus = -1;
        a = document.createElement("div");
        a.setAttribute("id", this.id + "autocomplete-list");
        a.setAttribute("class", "autocomplete-items");

        this.parentNode.appendChild(a);
        for (i = 0; i < arr.length; i++) {
            if (arr[i].substr(0, val.length).toUpperCase() == val.toUpperCase()) {
                b = document.createElement("DIV");
                b.innerHTML = "<strong>" + arr[i].substr(0, val.length) + "</strong>";
                b.innerHTML += arr[i].substr(val.length);
                b.innerHTML += "<input type='hidden' value='" + arr[i] + "'>";

                b.addEventListener("click", function(e) {
                    inp.value = this.getElementsByTagName("input")[0].value;

                    closeAllLists();
                });
                a.appendChild(b);
            }
        }
    });

    inp.addEventListener("keydown", function(e) {
        var x = document.getElementById(this.id + "autocomplete-list");
        if (x) x = x.getElementsByTagName("div");
        if (e.keyCode == 40) {
            //arrow DOWN key is pressed
            currentFocus++;
            addActive(x);
        } else if (e.keyCode == 38) {
            //arrow UP key is pressed
            currentFocus--;
            addActive(x);
        } else if (e.keyCode == 13) {
            //ENTER key is pressed
            e.preventDefault();
            if (currentFocus > -1) {
                /*and simulate a click on the "active" item:*/
                if (x) x[currentFocus].click();
            }
            let btn = document.querySelector(`.autocomplete button`);
            btn.click();
        }
    });

    function addActive(x) {
        if (!x) return false;

        for (var i = 0; i < x.length; i++) {
            x[i].classList.remove("autocomplete-active");
        }

        if (currentFocus >= x.length) currentFocus = 0;
        if (currentFocus < 0) currentFocus = (x.length - 1);

        x[currentFocus].classList.add("autocomplete-active");
    }

    function closeAllLists(elmnt) {
        var x = document.getElementsByClassName("autocomplete-items");
        for (var i = 0; i < x.length; i++) {
            if (elmnt != x[i] && elmnt != inp) {
                x[i].parentNode.removeChild(x[i]);
            }
        }
    }
    document.addEventListener("click", function (e) {
        closeAllLists(e.target);
    });
}


//get this list of suggestions
let suggestionsArray = new Array();
getSuggestions();
autocomplete(document.getElementById("myInput"), suggestionsArray);


//Speech section
let speechRecognition;
let micBtn = document.querySelector(".mic-btn");
let micBtnFlag = false;

let input = document.getElementById("myInput");
if ("webkitSpeechRecognition" in window) {
    // Initialize webkitSpeechRecognition
    speechRecognition = new webkitSpeechRecognition();
    // Set the properties for the Speech Recognition object
    speechRecognition.continuous = true;
    speechRecognition.interimResults = true;

    speechRecognition.addEventListener('result', function(e) {
        input.value = (e.results[0][0].transcript).replaceAll(".", "");

        if (e.results[0].isFinal) {
            // Stop the Speech Recognition
            speechRecognition.stop();

            //return the color of the mic after finish talking
            micBtn.classList.toggle("hover-color");
            micBtnFlag = false;
        }
    });

} else {
    console.log("Speech Recognition Not Available");
}

micBtn.addEventListener("click", () => {
    micBtnFlag = !micBtnFlag;
    if (micBtnFlag) {
        //change the color of mic while talking
        micBtn.classList.toggle("hover-color");

        // Start the Speech Recognition
        speechRecognition.start();
    }
});