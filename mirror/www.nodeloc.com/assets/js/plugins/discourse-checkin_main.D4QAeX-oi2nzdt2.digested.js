const{ajax:e}=window.moduleBroker.lookup("discourse/lib/ajax"),{popupAjaxError:n}=window.moduleBroker.lookup("discourse/lib/ajax-error"),{iconHTML:t}=window.moduleBroker.lookup("discourse/lib/icon-library"),{_INTERNAL_SOURCE_KEY:c}=window.moduleBroker.lookup("discourse/lib/api"),{withPluginApi:i}=window.moduleBroker.lookup("discourse/lib/plugin-api"),{i18n:o}=window.moduleBroker.lookup("discourse-i18n"),r=Object.freeze({type:"plugin",name:"discourse-checkin"})
var a={name:"checkin-header-button",initialize(a){const s=a.lookup("service:current-user")
s&&function(...e){const n="string"==typeof e[0]?2:1
e[n]={...e[n],[c]:r},i(...e)}("0.8.31",c=>{c.onPageChange(()=>{if(document.querySelector(".checkin-button"))return
const c=document.querySelector("ul.d-header-icons")
if(!c)return
const i=(()=>{const e=new Date
return`${e.getFullYear()}-${String(e.getMonth()+1).padStart(2,"0")}-${String(e.getDate()).padStart(2,"0")}`})(),r=`checkin-${s.id}-${i}`,a=!!localStorage.getItem(r),d=document.createElement("li")
d.className="header-dropdown-toggle checkin-icon"
const l=a?"checked-in":"",u=o(a?"js.checkin.already_checked_in":"js.checkin.check_in_button")
d.innerHTML=`\n          <button class="btn no-text btn-icon icon btn-flat checkin-button ${l}" \n                 title="${u}" \n                 aria-label="${u}">\n            ${t("calendar-heart")}\n            <span aria-hidden="true">&ZeroWidthSpace;</span>\n          </button>\n        `
const h=c.querySelector(".chat-header-icon")
h?c.insertBefore(d,h):c.appendChild(d)
const p=d.querySelector("button")
function k(e,n){const t=document.createElement("div")
t.className=`alert alert-${n} discourse-checkin-notification`,t.textContent=e,t.style.cssText="\n            position: fixed;\n            top: 60px;\n            right: 20px;\n            z-index: 9999;\n            padding: 10px 20px;\n            border-radius: 4px;\n            box-shadow: 0 2px 8px rgba(0,0,0,0.2);\n            animation: fadeInOut 3s ease-in-out;\n          "
const c=document.createElement("button")
c.innerHTML="&times;",c.style.cssText="\n            margin-left: 10px;\n            background: transparent;\n            border: none;\n            font-size: 18px;\n            cursor: pointer;\n            padding: 0 5px;\n          ",c.addEventListener("click",()=>{document.body.removeChild(t)}),t.appendChild(c)
const i=document.createElement("style")
i.textContent="\n            @keyframes fadeInOut {\n              0% { opacity: 0; transform: translateY(-20px); }\n              10% { opacity: 1; transform: translateY(0); }\n              90% { opacity: 1; }\n              100% { opacity: 0; }\n            }\n          ",document.head.appendChild(i),document.body.appendChild(t),setTimeout(()=>{t.parentNode&&document.body.removeChild(t),i.parentNode&&document.head.removeChild(i)},3e3)}p.addEventListener("click",async function(){if(a)k(o("js.checkin.already_checked_in"),"info")
else if(!p.disabled)try{p.disabled=!0
const n=Math.random().toString(36).substring(2,15)+Math.random().toString(36).substring(2,15),t=await e("/checkin",{type:"POST",headers:{"X-Discourse-Checkin":"true","X-Checkin-Nonce":n},data:{nonce:n,timestamp:Date.now()}})
if(t.success){p.classList.add("checked-in"),p.title=o("js.checkin.already_checked_in"),p.setAttribute("aria-label",o("js.checkin.already_checked_in"))
const e=t.user_date||i,n=`checkin-${s.id}-${e}`
localStorage.setItem(n,"true"),k(o("js.checkin.check_in_success",{points:t.points}),"success")}else k(t.message||o("js.checkin.error"),"warning"),t.message&&t.message.includes(o("js.checkin.already_checked_in"))&&(p.classList.add("checked-in"),p.title=o("js.checkin.already_checked_in"),p.setAttribute("aria-label",o("js.checkin.already_checked_in")),localStorage.setItem(r,"true"))}catch(e){k(e.jqXHR?.responseJSON?.message||o("js.checkin.error"),"error"),n(e)}finally{p.disabled=!1}})})})}}
const s={"discourse/initializers/checkin-header-button":Object.freeze({__proto__:null,default:a})}
export{s as default}

//# sourceMappingURL=../../map/plugins/discourse-checkin_main.D4QAeX-oi2nzdt2.digested.js.map
