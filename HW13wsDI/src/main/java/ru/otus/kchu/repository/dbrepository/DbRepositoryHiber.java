package ru.otus.kchu.repository.dbrepository;


import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;
import ru.otus.kchu.services.cache.CacheEngine;
import ru.otus.kchu.services.cache.MyElement;

import javax.persistence.Id;
import java.lang.reflect.Field;
import java.util.List;

@Repository
public class DbRepositoryHiber<K, T>implements DBRepository {
    int CashSize = 10;
    private  SessionFactory sessionFactory ;
    CacheEngine<Long, MyElement<K, T>> cache ;


    public DbRepositoryHiber() {
        sessionFactory = null;
    }

    public DbRepositoryHiber(SessionFactory sessionFactory, CacheEngine cache) {
        this.sessionFactory =sessionFactory;
        this.cache = cache;
    }

    public void init(SessionFactory sessionFactory, CacheEngine cache) {
        this.sessionFactory =sessionFactory;
        this.cache = cache;
    }
    @Override
    public <T> void create(T objectData) {

        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();

            session.save(objectData);
            System.out.println("-----------------created user:" + objectData );
            session.getTransaction().commit();
        }
    }
    @Override
    public <T> T load(long id, Class<T> clazz) {

        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();

            T record = session.get(clazz, id);
            System.out.println("-----------------new object:"+record);
            session.getTransaction().commit();
            return  record;
        }catch (Exception ex) {
            ex.printStackTrace();
            return null;

        }
    }

    @Override
    public <T> void update(T objectData) {

        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();

            session.update(objectData);
            session.getTransaction().commit();

            System.out.println("-----------------updated user:"+objectData);
        }
    }

    @Override
    public <T> void createOrUpdate(T objectData) {
        T loaded = getObject(objectData);

        try (Session session2 = sessionFactory.openSession()) {
        session2.beginTransaction();
            if(loaded == null) {
                session2.save(objectData);
            }
            else   {
                session2.update(objectData);
            }

            session2.getTransaction().commit();
            System.out.println("-----------------create/updated user:"+objectData);
        }

    }

    public <T> List <T> GetEntities( Class<T> clazz  ) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            String selectSQL =  "from "+clazz.getName();
            Query query;
           query = session.createQuery(selectSQL);

//            Query query = session.createQuery("from ContactEntity where firstName = :paramName");
//            query.setParameter("paramName", "Nick");
//            query.setParameter("paramName", "User1");
            List<T> resList = query.list();
            session.getTransaction().commit();
            System.out.println("All "+clazz.getSimpleName());
            resList.forEach(e-> System.out.println(e));
            return  resList;
        }catch (Exception ex) {
            ex.printStackTrace();
            return null;

        }
    }


    private <T> T getObject(T objectData) {
        Class clazz = objectData.getClass();
        Field idFld =checkId(clazz);
        T loaded = null;
        try (Session session = sessionFactory.openSession()) {

            loaded = (T) session.get(clazz, (long) idFld.get(objectData));
            } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return loaded;
    }

    private Field checkId(Class clazz) {

        for (Field field: clazz.getDeclaredFields()){
            field.setAccessible(true);
            if(field.isAnnotationPresent(Id.class)) {
                return  field;
            }
            field.setAccessible(false);
        }
        return  null;
    }
}
