const{getObjectForTheme:e}=window.moduleBroker.lookup("discourse/lib/theme-settings-store"),{_INTERNAL_SOURCE_KEY:o,apiInitializer:s}=window.moduleBroker.lookup("discourse/lib/api")
e(42)
const t=Object.freeze({type:"theme",id:42})
var r=function(...e){const r="string"==typeof e[0]?2:1
return e[r]={...e[r],[o]:t},s(...e)}(e=>{const o=e.getCurrentUser(),s=document.body
o?(s.classList.add("group-logged_in_users"),o.groups?.forEach(e=>{s.classList.add(`group-${e.name}`)})):s.classList.add("group-anonymous_users")})
const i={"discourse/api-initializers/init-theme":Object.freeze({__proto__:null,default:r})}
export{i as default}

//# sourceMappingURL=42185875bec7c98896d10ac9dc188e7c561cfc3c.map?__ws=www.nodeloc.com
