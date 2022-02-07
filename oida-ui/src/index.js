import Mirador from 'mirador/dist/es/src/index';
import annotationPlugins from 'mirador-annotations/es/index';
import LocalStorageAdapter from 'mirador-annotations/es/LocalStorageAdapter';

const state = {
    page: 0,
    page_size: 10,
    query: "",
    total_matches: 0
}

// Initialize mirador and ui
document.getElementById("mirador").style = "display: none;";
const mirador_viewer = Mirador.viewer({
    id: "mirador",
    window: {
        allowClose: true
    },
    workspaceControlPanel: {
        enabled: true
    },
    annotations: {
        filteredMotivations: ["oa:commenting", "oa:tagging", "sc:painting", "commenting", "tagging", "supplementing"]
    },
    annotation: {
        adapter: (canvasId) => new LocalStorageAdapter(`localStorage://?canvasId=${canvasId}`),
        exportLocalStorageAnnotations: false
    }
}, [...annotationPlugins]);

document.getElementById("queryform").addEventListener("submit", ev => {
    ev.preventDefault();
    console.log('blah');
    do_search(0);
    return false;
});

// First get total match count and then the results
function do_search(page) {
    state.query = document.getElementById("query").value;
    state.page = page;
    
    const count_url = new URL("/oida/doc/_size", window.location.origin);
    const query_url = new URL("/oida/doc/", window.location.origin);
    
    const filter = {
        "$text": {"$search": state.query}
    }
    
    count_url.searchParams.append("filter", JSON.stringify(filter));
    query_url.searchParams.append("filter", JSON.stringify(filter));
    query_url.searchParams.append("page", state.page + 1);
    query_url.searchParams.append("pagesize", state.page_size);
    
    fetch(count_url).then(x => x.json()).then(x => {
        state.total_matches = x._size;
        fetch(query_url).then(x => x.json()).then(x => display_result(x));   
    });
}

function create_element(parent, name, classes = "") {
    const el = document.createElement(name);
    el.className = classes;
    parent.appendChild(el);
    return el;
}

function display_result(result) {
    const results_div = document.getElementById("results");
    
    // Clear results
    while (results_div.firstChild) {
        results_div.removeChild(results_div.firstChild);
    }
    
    const h3 = create_element(results_div, "h3");
    h3.appendChild(document.createTextNode("Total matches: " + state.total_matches));
    
    // List of matches
    
    const ul = create_element(results_div, "ul", "list-group");
    ul.style = "width: 20rem;"
    let num = (state.page * state.page_size) + 1;
    
    for (const d of result) {
        let li = create_element(ul, "li", "list-group-item");
        
        create_element(li, "strong").appendChild(document.createTextNode(num++ + ". "));
        
        li.appendChild(document.createTextNode(d._id));
        
        let img = create_element(li, "img");
        img.src = d.pages[0].iiif_image + "/full/!128,128/0/default.jpg";
        img.addEventListener("click", ev => {
            show_doc(d.iiif_manifest);
            return false;
        });
    }
    
    // Pagination
    
    const nav  = create_element(results_div, "nav");
    nav.ariaLabel = "...";
    
    const nav_ul  = create_element(nav, "ul", "pagination");
    
    let page_count = Math.floor(state.total_matches / state.page_size);
    if (state.total_matches % state.page_size != 0) {
        page_count++;
    }
    
    // Previous

    {
        const li = create_element(nav_ul, "li");
        const a = create_element(li, "a", "page-link");
        a.href = "#";              
        
        if (state.page == 0) {
            li.className = "page-item disabled";
            a.tabIndex = "-1";
        } else {
            handle_pagination_click(a, state.page - 1);                  
            li.className = "page-item";                  
        }
        a.appendChild(document.createTextNode("Previous"));
    }
    
    // Pages
    
    for (let i = 0; i < page_count; i++) {
        let li = document.createElement("li");
        
        if (i  === state.page) {
            li.className = "page-item active";
        } else {
            li.className = "page-item";
        }
        
        const a = create_element(li, "a", "page-link");              
        a.href = "#";
        a.appendChild(document.createTextNode(i + 1));
        handle_pagination_click(a, i);
        
        nav_ul.appendChild(li);
    }
    
    // Next

    {
        const li = create_element(nav_ul, "li");
        const a = create_element(li, "a", "page-link");
        a.href = "#";              
        
        if (state.page + 1 >= page_count ) {
            li.className = "page-item disabled";
            a.tabIndex = "-1";
        } else {
            li.className = "page-item";
            handle_pagination_click(a, state.page + 1);
        }
        a.appendChild(document.createTextNode("Next"));
    }
    
    document.getElementById("mirador").style = "display: none;";
}

function handle_pagination_click(a, page) {
    a.addEventListener("click", ev => {
        do_search(page);
        return false;
    });
}

function show_doc(manifest_url) {
    document.getElementById("mirador").style = "position: relative; width: 100%; height: 75vh; display: block;";
    
    let action = Mirador.actions.addWindow({ manifestId: manifest_url })
    mirador_viewer.store.dispatch(action);
}
