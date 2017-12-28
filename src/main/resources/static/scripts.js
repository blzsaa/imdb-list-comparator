function hideWhenMovieTableIsEmpty(index) {
if(document.getElementById("movieTable"+index).getElementsByTagName("tr").length <= 1)
 document.getElementById("movieTable"+index).style.display= "none";
}

function filterFunction(listIndex) {
  var input, filter, table, tr, td, i;
  input = document.getElementById("myInput");
  filter = input.value.toUpperCase();
  table = document.getElementById("movieTable"+listIndex);
  tr = table.getElementsByTagName("tr");
  for (i = 0; i < tr.length; i++) {
    td = tr[i].getElementsByTagName("td")[0];
    if (td) {
      if (td.innerHTML.toUpperCase().indexOf(filter) > -1) {
        tr[i].style.display = "";
      } else {
        tr[i].style.display = "none";
      }
    }
  }
}