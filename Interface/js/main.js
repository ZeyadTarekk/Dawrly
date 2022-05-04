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



  if (pagesNum == 0) {
    //add there is no results to show
    let noResults = document.createElement("div");
    noResults.className = "no-results";
    noResults.innerHTML = "<p>Oops, there is no results to show</p>";
    searchResults.appendChild(noResults);

  } else if (pagesNum == 1) {
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

  if (pagesNum == 0) {
    let numbers = document.querySelector(".numbers");
    numbers.classList.add("hidden");
  } else {
    //add numbers to move between the pages
    let numbersDiv = document.querySelector(`.pages-numbers`);
    numbersDiv.children[0].children[0].addEventListener("click", function(e) {
      changePages(1, e);
    });
    for (let i = 2; i <= counter; i++) {
      let span = document.createElement("span");
      span.classList.add("r-char");

      span.innerHTML = "r <a>" + i + "</a>";
      span.children[0].addEventListener("click", function(e) {
        changePages(i, e);
      });
      numbersDiv.appendChild(span);
    };
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
      if (currentPage != 1) {
        let prev = document.querySelector(`.numbers .previous-btn`);
        prev.classList.remove("hidden");
      } else {
        let prev = document.querySelector(`.numbers .previous-btn`);
        prev.classList.add("hidden");
      }

      if (currentPage == counter) {
        let next = document.querySelector(`.numbers .next-btn`);
        next.classList.add("hidden");
      } else {
        let next = document.querySelector(`.numbers .next-btn`);
        next.classList.remove("hidden");
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
    }

    document.body.scrollTop = 0; // For Safari
    document.documentElement.scrollTop = 0; // For Chrome, Firefox, IE and Opera
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