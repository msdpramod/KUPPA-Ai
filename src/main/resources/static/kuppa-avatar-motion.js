(()=>{
  const query=typeof window.matchMedia==='function'
    ? window.matchMedia('(prefers-reduced-motion: reduce)')
    : null;
  let reduced=!!query?.matches;

  const FULL_MOTION_SCALE=Object.freeze({
    IDLE:.75,
    NOTICED_USER:1.2,
    LISTENING:.45,
    UNDERSTANDING:.55,
    ASKING_VAYU:.45,
    THINKING:.42,
    RESPONDING:.8,
    SPEAKING:1,
    WAITING:.65
  });

  function publish(){
    document.documentElement.dataset.avatarMotion=reduced?'reduced':'full';
    window.dispatchEvent(new CustomEvent('kuppa-motion-preference-change',{
      detail:{version:'v1',reduced}
    }));
  }

  function onChange(event){
    reduced=!!event.matches;
    publish();
  }

  if(query){
    if(typeof query.addEventListener==='function')query.addEventListener('change',onChange);
    else if(typeof query.addListener==='function')query.addListener(onChange);
  }

  const api=Object.freeze({
    version:'v1',
    isReduced(){return reduced},
    autonomousScale(state){return reduced?0:(FULL_MOTION_SCALE[state]??.7)},
    gazeScale(){return reduced?.25:1}
  });

  Object.defineProperty(globalThis,'KuppaAvatarMotionPolicy',{value:api,writable:false,configurable:false});
  publish();
})();
