/*
 * The MIT License
 *
 * Copyright 2015 mazuh.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package br.com.fanex.mazuh.jpa;

import br.com.fanex.mazuh.acesso.Hierarquia;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import br.com.fanex.mazuh.acesso.Usuario;
import br.com.fanex.mazuh.exceptions.NonexistentEntityException;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author mazuh
 */
public class HierarquiaJpaController implements Serializable {

    public HierarquiaJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Hierarquia hierarquia) {
        if (hierarquia.getUsuarioList() == null) {
            hierarquia.setUsuarioList(new ArrayList<Usuario>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            List<Usuario> attachedUsuarioList = new ArrayList<Usuario>();
            for (Usuario usuarioListUsuarioToAttach : hierarquia.getUsuarioList()) {
                usuarioListUsuarioToAttach = em.getReference(usuarioListUsuarioToAttach.getClass(), usuarioListUsuarioToAttach.getId());
                attachedUsuarioList.add(usuarioListUsuarioToAttach);
            }
            hierarquia.setUsuarioList(attachedUsuarioList);
            em.persist(hierarquia);
            for (Usuario usuarioListUsuario : hierarquia.getUsuarioList()) {
                Hierarquia oldIdCategoriaOfUsuarioListUsuario = usuarioListUsuario.getIdCategoria();
                usuarioListUsuario.setIdCategoria(hierarquia);
                usuarioListUsuario = em.merge(usuarioListUsuario);
                if (oldIdCategoriaOfUsuarioListUsuario != null) {
                    oldIdCategoriaOfUsuarioListUsuario.getUsuarioList().remove(usuarioListUsuario);
                    oldIdCategoriaOfUsuarioListUsuario = em.merge(oldIdCategoriaOfUsuarioListUsuario);
                }
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Hierarquia hierarquia) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Hierarquia persistentHierarquia = em.find(Hierarquia.class, hierarquia.getId());
            List<Usuario> usuarioListOld = persistentHierarquia.getUsuarioList();
            List<Usuario> usuarioListNew = hierarquia.getUsuarioList();
            List<Usuario> attachedUsuarioListNew = new ArrayList<Usuario>();
            for (Usuario usuarioListNewUsuarioToAttach : usuarioListNew) {
                usuarioListNewUsuarioToAttach = em.getReference(usuarioListNewUsuarioToAttach.getClass(), usuarioListNewUsuarioToAttach.getId());
                attachedUsuarioListNew.add(usuarioListNewUsuarioToAttach);
            }
            usuarioListNew = attachedUsuarioListNew;
            hierarquia.setUsuarioList(usuarioListNew);
            hierarquia = em.merge(hierarquia);
            for (Usuario usuarioListOldUsuario : usuarioListOld) {
                if (!usuarioListNew.contains(usuarioListOldUsuario)) {
                    usuarioListOldUsuario.setIdCategoria(null);
                    usuarioListOldUsuario = em.merge(usuarioListOldUsuario);
                }
            }
            for (Usuario usuarioListNewUsuario : usuarioListNew) {
                if (!usuarioListOld.contains(usuarioListNewUsuario)) {
                    Hierarquia oldIdCategoriaOfUsuarioListNewUsuario = usuarioListNewUsuario.getIdCategoria();
                    usuarioListNewUsuario.setIdCategoria(hierarquia);
                    usuarioListNewUsuario = em.merge(usuarioListNewUsuario);
                    if (oldIdCategoriaOfUsuarioListNewUsuario != null && !oldIdCategoriaOfUsuarioListNewUsuario.equals(hierarquia)) {
                        oldIdCategoriaOfUsuarioListNewUsuario.getUsuarioList().remove(usuarioListNewUsuario);
                        oldIdCategoriaOfUsuarioListNewUsuario = em.merge(oldIdCategoriaOfUsuarioListNewUsuario);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = hierarquia.getId();
                if (findHierarquia(id) == null) {
                    throw new NonexistentEntityException("The hierarquia with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Hierarquia hierarquia;
            try {
                hierarquia = em.getReference(Hierarquia.class, id);
                hierarquia.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The hierarquia with id " + id + " no longer exists.", enfe);
            }
            List<Usuario> usuarioList = hierarquia.getUsuarioList();
            for (Usuario usuarioListUsuario : usuarioList) {
                usuarioListUsuario.setIdCategoria(null);
                usuarioListUsuario = em.merge(usuarioListUsuario);
            }
            em.remove(hierarquia);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Hierarquia> findHierarquiaEntities() {
        return findHierarquiaEntities(true, -1, -1);
    }

    public List<Hierarquia> findHierarquiaEntities(int maxResults, int firstResult) {
        return findHierarquiaEntities(false, maxResults, firstResult);
    }

    private List<Hierarquia> findHierarquiaEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Hierarquia.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Hierarquia findHierarquia(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Hierarquia.class, id);
        } finally {
            em.close();
        }
    }

    public int getHierarquiaCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Hierarquia> rt = cq.from(Hierarquia.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
