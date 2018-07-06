package cohesivo.boardactivity.app;

import android.app.Application;

import java.util.concurrent.atomic.AtomicInteger;

import cohesivo.boardactivity.models.Board;
import cohesivo.boardactivity.models.Note;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import io.realm.RealmResults;

/**
 * Created by ADMIN on 13/06/2018.
 */

public class MyApplication extends Application {

    public static AtomicInteger BoardID = new AtomicInteger();
    public static AtomicInteger NoteID = new AtomicInteger();

    @Override
    public void onCreate() {
        super.onCreate();

        setUpRealmConfig();
        Realm realm = Realm.getDefaultInstance();

        BoardID = getIdByTable(realm, Board.class);
        NoteID = getIdByTable(realm, Note.class);
        realm.close();
    }

    private void setUpRealmConfig(){
        RealmConfiguration config = new RealmConfiguration
                .Builder(getApplicationContext())
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);
    }

    //clase genérica (T)
    private <T extends RealmObject> AtomicInteger getIdByTable(Realm realm, Class<T> anyClass){
        //consultar todos los registros de una tabla (anyClass)
        RealmResults<T> results = realm.where(anyClass).findAll();
        //Si es mayor a cero devuelva el maximo id de la tabla si no, sin pasar cero a
        // AtomicInteger significa cero (0).
        return (results.size() > 0) ? new AtomicInteger(results.max("id").intValue()) : new AtomicInteger();
    }

}
