const{_INTERNAL_SOURCE_KEY:i}=window.moduleBroker.lookup("discourse/lib/api"),{withPluginApi:n}=window.moduleBroker.lookup("discourse/lib/plugin-api"),e=Object.freeze({type:"plugin",name:"discourse-github"})
var o={name:"github-admin-plugin-configuration-nav",initialize(o){const u=o.lookup("service:current-user")
u?.admin&&function(...o){const u="string"==typeof o[0]?2:1
o[u]={...o[u],[i]:e},n(...o)}(i=>{i.setAdminPluginIcon("discourse-github","fab-github")})}}
const u={"discourse/initializers/github-admin-plugin-configuration-nav":Object.freeze({__proto__:null,default:o})}
export{u as default}

//# sourceMappingURL=../../map/plugins/discourse-github_main.DPzaQZ-4dom2mgr.digested.js.map
