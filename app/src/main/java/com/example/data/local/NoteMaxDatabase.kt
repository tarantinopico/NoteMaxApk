package com.example.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.util.Converters

@Database(
    entities = [FolderEntity::class, NoteEntity::class, NoteFtsEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class NoteMaxDatabase : RoomDatabase() {
    abstract fun folderDao(): FolderDao
    abstract fun noteDao(): NoteDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE VIRTUAL TABLE IF NOT EXISTS `notes_fts` USING FTS4(`title`, `content`, `tags`, content=`notes`)")
                db.execSQL("CREATE TRIGGER IF NOT EXISTS `notes_fts_insert` AFTER INSERT ON `notes` BEGIN INSERT INTO `notes_fts`(`rowid`, `title`, `content`, `tags`) VALUES (new.`rowid`, new.`title`, new.`content`, new.`tags`); END")
                db.execSQL("CREATE TRIGGER IF NOT EXISTS `notes_fts_update` AFTER UPDATE ON `notes` BEGIN UPDATE `notes_fts` SET `title` = new.`title`, `content` = new.`content`, `tags` = new.`tags` WHERE `rowid` = old.`rowid`; END")
                db.execSQL("CREATE TRIGGER IF NOT EXISTS `notes_fts_delete` AFTER DELETE ON `notes` BEGIN DELETE FROM `notes_fts` WHERE `rowid` = old.`rowid`; END")
            }
        }
    }
}
