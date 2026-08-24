const o={"lib/discourse-markdown/bbcode-color":Object.freeze({__proto__:null,setup:function(o){o.allowList({custom(o,t,e){if("span"===o&&"style"===t)return/^(background-)?color:#?[a-zA-Z0-9]+$/.exec(e)}}),o.registerOptions(o=>{o.features["bbcode-color"]=!0}),o.markdownIt?o.registerPlugin(o=>{const t=o.inline.bbcode.ruler
t.push("bgcolor",{tag:"bgcolor",wrap:function(o,t,e){o.type="span_open",o.tag="span",o.attrs=[["style","background-color:"+e.attrs._default.trim()]],o.content="",o.nesting=1,t.type="span_close",t.tag="span",t.nesting=-1,t.content=""}}),t.push("color",{tag:"color",wrap:function(o,t,e){o.type="span_open",o.tag="span",o.attrs=[["style","color:"+e.attrs._default.trim()]],o.content="",o.nesting=1,t.type="span_close",t.tag="span",t.nesting=-1,t.content=""}})}):(o.addPreProcessor(o=>function(o){let t
o||=""
do{t=o,o=o.replace(/\[color=([^\]]+)\]((?:(?!\[color=[^\]]+\]|\[\/color\])[\S\s])*)\[\/color\]/gi,(o,t,e)=>`<span style='color:${t}'>${e}</span>`)}while(o!==t)
return o}(o)),o.addPreProcessor(o=>function(o){let t
o||=""
do{t=o,o=o.replace(/\[bgcolor=([^\]]+)\]((?:(?!\[bgcolor=[^\]]+\]|\[\/bgcolor\])[\S\s])*)\[\/bgcolor\]/gi,(o,t,e)=>`<span style='background-color:${t}'>${e}</span>`)}while(o!==t)
return o}(o)))}})}
export{o as default}

//# sourceMappingURL=../../map/plugins/discourse-bbcode-color_main.ByYeBg-jivo81c9.digested.js.map
