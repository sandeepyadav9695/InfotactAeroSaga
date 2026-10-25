import React,{useEffect,useMemo,useRef,useState}from'react';
const API=import.meta.env.VITE_API_BASE||'http://localhost:8081';
const WS=import.meta.env.VITE_WS_URL||'ws://localhost:8081/ws/telemetry';
export default function App(){
 const mapRef=useRef(null),viewerRef=useRef(null),entities=useRef(new Map());
 const[drones,setDrones]=useState([]),
 [connection,setConnection]=useState('CONNECTING'),
 [busy,setBusy]=useState(false),
 [message,setMessage]=useState(''),
 [filter,setFilter]=useState('ALL'),
 [tick,setTick]=useState(0);
 useEffect(()=>{
     const C=window.Cesium;
     if(!C||!mapRef.current){setMessage('CesiumJS failed to load. Check internet access.');
         return
         }
 const v=new C.Viewer(mapRef.current,{
     imageryProvider:new C.UrlTemplateImageryProvider({
         url:'https://tile.openstreetmap.org/{z}/{x}/{y}.png'}),
         terrainProvider:new C.EllipsoidTerrainProvider(),
         animation:false,
         timeline:false,
         geocoder:false,
         homeButton:true,
         sceneModePicker:false,
         baseLayerPicker:false,
         navigationHelpButton:false,
         fullscreenButton:true,
         infoBox:false,
         selectionIndicator:false});
 v.camera.flyTo({destination:C.Cartesian3.fromDegrees(-122.4194,37.7749,45000)});
 viewerRef.current=v;
 return()=>{v.destroy();viewerRef.current=null;entities.current.clear()}},[]);

 useEffect(()=>{
     let ws,timer,disposed=false;
     const connect=()=>{if(disposed)return;
         ws=new WebSocket(WS);
         ws.onopen=()=>setConnection('CONNECTED');
         ws.onclose=()=>{setConnection('RECONNECTING');
             if(!disposed)timer=setTimeout(connect,1500)};
             ws.onerror=()=>ws.close();
             ws.onmessage=e=>{try{const p=JSON.parse(e.data);
                 if(p.type!=='telemetry')return;setDrones(p.drones||[]);
                 setTick(p.tick||0);
                 const C=window.Cesium,v=viewerRef.current;
                 if(!C||!v)return;
                 for(const d of p.drones||[]){
                     let entity=entities.current.get(d.droneId);
                     const color=d.status==='OFFLINE'?C.Color.RED:d.status==='DELIVERING'?C.Color.ORANGE:C.Color.CYAN;
                     const pos=C.Cartesian3.fromDegrees(d.longitude,d.latitude,d.altitude);
                     if(!entity){
                         entity=v.entities.add({
                             id:d.droneId,position:pos,point:{
                                 pixelSize:9,color,outlineColor:C.Color.WHITE,outlineWidth:1},
                                 label:{text:d.droneId
                                     ,font:'10px sans-serif',
                                     fillColor:C.Color.WHITE,
                                     outlineColor:C.Color.BLACK,
                                     outlineWidth:2,
                                     style:C.LabelStyle.FILL_AND_OUTLINE,
                                     pixelOffset:new C.Cartesian2(0,-14),
                                     distanceDisplayCondition:new C.DistanceDisplayCondition(0,90000)}});

                                     entities.current.set(d.droneId,entity)}else{entity.position=pos;
                                         entity.point.color=color}}}
                                         catch(err){
                                             console.error('Bad telemetry payload',err)}}};
                                             connect();
                                             return()=>{disposed=true;clearTimeout(timer);
     if(ws)ws.close()}},[]);
 const counts=useMemo(()=>({
     total:drones.length,
     flying:drones.filter(d=>d.status==='IN_FLIGHT').length,delivering:drones.filter(d=>d.status==='DELIVERING').length,
     offline:drones.filter(d=>d.status==='OFFLINE').length}),[drones]);
 const visible=filter==='ALL'?drones:drones.filter(d=>d.status===filter);
 async function startFleet(){
     setBusy(true);setMessage('Starting fleet workflow…');
     try{
         const ids=Array.from({length:10},(_,i)=>`DRONE-${String(i+1).padStart(3,'0')}`);
         const r=await fetch(`${API}/api/missions/fleet/start`,{
             method:'POST',
             headers:{'Content-Type':'application/json'},
             body:JSON.stringify({droneIds:ids})});
             const data=await r.json();
             if(!r.ok)throw Error(data.error||'Request failed');
             setMessage(`Started ${data.count} child missions. Fleet workflow: ${data.workflowId}`)
             }
             catch(e){
                 setMessage(`Could not start fleet: ${e.message}. Check Temporal and Spring Boot.`)
                 }
             finally{setBusy(false)
                 }
             }
 return <main className="shell"><header className="top">
     <div>
         <small>AEROSAGA / TEMPORAL.IO AUTONOMOUS OPERATIONS</small>
 <h1>AeroSaga<span>Fleet Control</span></h1>
     </div>
     <div className={'connection '+connection.toLowerCase()}><i/>{connection}</div>
     </header>
 <section className="stats">
     <Stat label="CONNECTED DRONES" value={counts.total} note="Live telemetry"/>
     <Stat label="IN FLIGHT" value={counts.flying} note="En route"/>
     <Stat label="DELIVERING" value={counts.delivering} note="Package drop"/>
     <Stat label="OFFLINE" value={counts.offline} note="Simulated signal loss"/>
     </section>
 <section className="workspace">
     <div className="panel mapPanel">
         <div className="heading">
             <div>
                 <h2>Live 3D fleet</h2>
                 <p>Mock GPS · telemetry tick {tick}</p>
                 </div>
                 <b className="live">● LIVE</b>
                 </div>
                 <div className="map" ref={mapRef}/>
                 <div className="legend">
                     <span>🔵 In flight</span>
                     <span>🟠 Delivering</span>
                     <span>🔴 Offline</span>
                     </div>
                     </div>
 <aside className="panel fleet">
     <div className="heading">
         <div>
             <h2>Fleet inventory</h2>
             <p>{visible.length} drones displayed</p>
             </div>
             <select value={filter} onChange={e=>setFilter(e.target.value)}>
                 <option value="ALL">All status</option>
                 <option value="IN_FLIGHT">In flight</option>
                 <option value="DELIVERING">Delivering</option>
                 <option value="OFFLINE">Offline</option>
                 </select>
                 </div>
                 <div className="list">{visible.map(d=>
                     <div className="drone" key={d.droneId}>
                         <span className={'glyph '+d.status.toLowerCase()}>✦</span>
                         <div className="details">
                             <b>{d.droneId}</b>
                             <small>{d.latitude.toFixed(4)}, {d.longitude.toFixed(4)}</small>
                             </div>
 <span className={'status '+d.status.toLowerCase()}>{d.status.replace('_',' ')}</span>
 </div>)}
 </div>
 <button className="primary" disabled={busy} onClick={startFleet}>{busy?'Starting…':'▶  Start 10-drone workflow'}</button>
 <p className="hint">Starts concurrent Temporal child workflows. GPS simulation runs independently.</p>
 </aside>
 </section>
 {message&&
     <div className="toast" role="status">{message}
         <button onClick={()=>setMessage('')}>×</button>
         </div>}
         <footer>
             <span>WEEK 02 BUILD</span>
             <span>DURABLE WORKFLOWS · WEBSOCKET TELEMETRY · CESIUMJS</span>
             </footer>
             </main>
}
function Stat({label,value,note}){
    return <div className="stat"><small>{label}</small><strong>{value}</strong><span>{note}</span></div>}
