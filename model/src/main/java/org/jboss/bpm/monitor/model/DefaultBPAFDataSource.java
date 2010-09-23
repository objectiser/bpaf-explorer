/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.bpm.monitor.model;

import org.jboss.bpm.monitor.model.bpaf.Event;
import org.jboss.bpm.monitor.model.metric.Timespan;

import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.transaction.*;
import java.util.List;

/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Mar 10, 2010
 */
public class DefaultBPAFDataSource implements BPAFDataSource
{

    EntityManagerFactory emf;
    
    public DefaultBPAFDataSource(EntityManagerFactory emf) {
        this.emf = emf;
    }

    private interface SQLCommand<T>
    {
        T execute(EntityManager em);
    }

    private <T> T executeCommand(SQLCommand<T> cmd)
    {

        EntityManager em = null;
        UserTransaction tx = null;
        boolean sucess = true;

        try
        {
            InitialContext ctx = new InitialContext();
            tx = (UserTransaction)ctx.lookup("UserTransaction");
            tx.begin();

            em = emf.createEntityManager();         
            return cmd.execute(em);
        }
        catch(Exception e)
        {
            sucess = false;
            throw new RuntimeException("Failed to execute query", e);
        }
        finally
        {
            if(em!=null)
            {
                try {
                    if(sucess)
                        tx.commit();
                    else
                        tx.setRollbackOnly();
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                em.close();
            }
        }

    }

    public List<String> getProcessDefinitions()
    {
        List<String> result = executeCommand(new SQLCommand<List<String>>()
        {
            public List<String> execute(EntityManager em)
            {
                Query query = em.createQuery(
                        "select distinct e.processDefinitionID from org.jboss.bpm.monitor.model.bpaf.Event as e"
                );
                return query.getResultList();
            }
        });

        return result;
    }

    public List<String> getProcessInstances(final String processDefinition)
    {
        List<String> result = executeCommand(new SQLCommand<List<String>>()
        {
            public List<String> execute(EntityManager em)
            {
                Query query = em.createQuery(
                        "select distinct e.processInstanceID from org.jboss.bpm.monitor.model.bpaf.Event as e" +
                                " where e.processDefinitionID=:id"
                );
                query.setParameter("id", processDefinition);
                return query.getResultList();
            }
        });

        return result;
    }

    public List<String> getActivityDefinitions(final String processInstance)
    {
        List<String> result = executeCommand(new SQLCommand<List<String>>()
        {
            public List<String> execute(EntityManager em)
            {
                Query query = em.createQuery(
                        "select distinct e.activityDefinitionID from org.jboss.bpm.monitor.model.bpaf.Event as e" +
                                " where e.processInstanceID=:id" +
                                " and e.activityDefinitionID!=null"
                );
                query.setParameter("id", processInstance);
                return query.getResultList();
            }
        });

        return result;
    }

    public List<Event> getDefinitionEvents(final String processDefinition, final Timespan timespan)
    {
        List<Event> result = executeCommand(new SQLCommand<List<Event>>()
        {
            public List<Event> execute(EntityManager em)
            {

                Query query = em.createNativeQuery("select e1.* " +
                        "from BPAF_EVENT e1, BPAF_EVENT e2 " +
                        "where e1.processDefinitionID=e2.processDefinitionID " +
                        "and e1.processInstanceID=e2.processInstanceID " +
                        "and ((e1.currentState=\"Open_Running\" and e2.currentState=\"Closed_Completed\") OR (e2.currentState=\"Open_Running\" and e1.currentState=\"Closed_Completed\")) " +
                        "and e1.activityDefinitionID is null " +
                        "and e2.activityDefinitionID is null " +
                        "and e1.processDefinitionID='"+processDefinition+"' "+
                        "and e1.timeStamp>="+timespan.getStart()+" "+
                        "and e2.timeStamp<="+timespan.getEnd()+" "+
                        "order by e1.eventID;", Event.class);

                return query.getResultList();
            }
        });

        System.out.println(timespan.getTitle() +": "+ result.size());
        return result;
    }

    public List<Event> getInstanceEvents(final String... processInstances)
    {
        List<Event> result = executeCommand(new SQLCommand<List<Event>>()
        {
            public List<Event> execute(EntityManager em)
            {
                StringBuffer sb = new StringBuffer("SELECT e1.* ");
                sb.append("FROM BPAF_EVENT e1, BPAF_EVENT e2 ");
                sb.append("WHERE e1.processInstanceID=e2.processInstanceID " );
                sb.append("AND ((e1.currentState=\"Open_Running\" and e2.currentState=\"Closed_Completed\") OR (e2.currentState=\"Open_Running\" and e1.currentState=\"Closed_Completed\")) " );
                sb.append("AND e1.activityDefinitionID is not null " );
                sb.append("AND e2.activityDefinitionID is not null " );

                sb.append("AND (");
                for(int i=0; i<processInstances.length; i++)
                {
                    if(i==0)
                        sb.append("e1.processInstanceID=\""+processInstances[i]+"\" ");
                    else
                        sb.append("OR e1.processInstanceID=\""+processInstances[i]+"\" ");
                }

                sb.append(") ");

                //sb.append("and e1.timeStamp>="+timespan.getStart()+" ");
                //sb.append("and e2.timeStamp<="+timespan.getEnd()+" ");

                sb.append("GROUP BY e1.activityInstanceID " );
                sb.append("ORDER BY e1.timeStamp, e1.processInstanceID");


                Query query = em.createNativeQuery(sb.toString(), Event.class);

                return query.getResultList();
            }
        });

        return result;
    }
}
