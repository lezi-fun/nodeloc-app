const{_INTERNAL_SOURCE_KEY:i}=window.moduleBroker.lookup("discourse/lib/api"),{withPluginApi:o}=window.moduleBroker.lookup("discourse/lib/plugin-api"),n=Object.freeze({type:"plugin",name:"discourse-narrative-bot"})
var e={name:"narrative-bot-admin-plugin-configuration-nav",initialize(e){const r=e.lookup("service:current-user")
r?.admin&&function(...e){const r="string"==typeof e[0]?2:1
e[r]={...e[r],[i]:n},o(...e)}(i=>{i.setAdminPluginIcon("discourse-narrative-bot","robot")})}}
const r={"discourse/initializers/narrative-bot-admin-plugin-configuration-nav":Object.freeze({__proto__:null,default:e})}
export{r as default}

//# sourceMappingURL=../../map/plugins/discourse-narrative-bot_main.D260cb-hnuxjp3y.digested.js.map
