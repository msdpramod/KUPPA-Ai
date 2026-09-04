(()=>{
  const stage=document.getElementById('stage');
  const brainStatus=document.getElementById('brainStatus');
  if(!stage||!brainStatus)return;

  const PROCESSING_STATES=new Set(['UNDERSTANDING','ASKING_VAYU','THINKING','RESPONDING']);
  const PRESENCE_BY_STATE=Object.freeze({
    IDLE:'calm',NOTICED_USER:'engaged',LISTENING:'attentive',UNDERSTANDING:'processing',
    ASKING_VAYU:'processing',THINKING:'processing',RESPONDING:'engaged',SPEAKING:'expressive',WAITING:'calm'
  });
  let pendingSince=0;
  let timer=null;

  function stopLatencyClock(){
    if(timer){clearInterval(timer);timer=null}
    pendingSince=0;
    delete stage.dataset.latency;
  }

  function renderLatency(){
    if(!pendingSince||document.hidden)return;
    const elapsed=Math.max(0,performance.now()-pendingSince);
    const seconds=(elapsed/1000).toFixed(1);
    stage.dataset.latency=elapsed>=4000?'slow':'active';
    brainStatus.textContent=elapsed>=4000?`Vayu · thinking · ${seconds}s · still working`:`Vayu · thinking · ${seconds}s`;
  }

  function startLatencyClock(){
    stopLatencyClock();
    pendingSince=performance.now();
    renderLatency();
    timer=setInterval(renderLatency,250);
  }

  function applyState(next){
    const state=next||stage.dataset.state||'IDLE';
    stage.dataset.presence=PRESENCE_BY_STATE[state]||'calm';
    stage.setAttribute('aria-busy',String(PROCESSING_STATES.has(state)));
  }

  stage.addEventListener('kuppa-state-change',event=>applyState(event.detail?.to));
  stage.addEventListener('kuppa-brain-state-change',event=>{
    if(event.detail?.mode==='pending')startLatencyClock();
    else stopLatencyClock();
  });
  document.addEventListener('visibilitychange',()=>{if(!document.hidden&&pendingSince)renderLatency()});

  const api=Object.freeze({
    version:'v1',
    getPresence(){return stage.dataset.presence||'calm'},
    isBusy(){return stage.getAttribute('aria-busy')==='true'}
  });
  Object.defineProperty(globalThis,'KuppaPresenceController',{value:api,writable:false,configurable:false,enumerable:false});
  applyState(stage.dataset.state);
  stage.dispatchEvent(new CustomEvent('kuppa-presence-controller-ready',{detail:{version:api.version}}));
})();
