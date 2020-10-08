select  l.processoperationname,p.DESCRIPTION, count(*)
 from lot l ,processoperationspec p 
 where l.PROCESSOPERATIONNAME = p.PROCESSOPERATIONNAME and l.nodestack is null
 group by l.processoperationname,p.DESCRIPTION
 order by l.processoperationname;
 
 update lot a set a.nodestack=(select n.nodeid from node n where a.processoperationname=n.NODEATTRIBUTE1 and a.PROCESSFLOWNAME=n.PROCESSFLOWNAME)
where a.nodestack is null;

