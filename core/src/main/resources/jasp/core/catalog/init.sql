-- common function to update modified timestamp on update
CREATE OR REPLACE FUNCTION %scope%update_last_modified()
RETURNS TRIGGER AS $$
BEGIN
    NEW.modified = now();
    RETURN NEW;
END;
$$ language 'plpgsql';;

-- function to update containing folder modified
CREATE OR REPLACE FUNCTION %scope%update_folder_last_modified()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE %scope%jasp_folder SET modified = NEW.modified WHERE id = NEW.folder_id;
    RETURN NEW;
END;
$$ language 'plpgsql';;

-- metadata table
CREATE TABLE IF NOT EXISTS %scope%jasp_info (
    version VARCHAR NOT NULL,
    revision VARCHAR NOT NULL,
    meta JSON
);;

-- dataset table
CREATE TABLE IF NOT EXISTS %scope%jasp_dataset (
    id UUID PRIMARY KEY,
    name VARCHAR NOT NULL,
    title VARCHAR,
    description VARCHAR,
    query VARCHAR NOT NULL,
    params JSON,
    creator VARCHAR NOT NULL DEFAULT current_user,
    created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    modified TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    tags VARCHAR[],
    meta JSON
);;

-- trigger on dataset to update modified timestamp
DROP TRIGGER IF EXISTS update_dataset_modified ON %scope%jasp_dataset;;
CREATE TRIGGER update_dataset_modified
  BEFORE UPDATE ON %scope%jasp_dataset
    FOR EACH ROW EXECUTE PROCEDURE %scope%update_last_modified();;

-- folder table
CREATE TABLE IF NOT EXISTS %scope%jasp_folder (
    id UUID PRIMARY KEY,
    name VARCHAR NOT NULL,
    title VARCHAR,
    description VARCHAR,
    creator VARCHAR NOT NULL DEFAULT current_user,
    created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    modified TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    tags VARCHAR[],
    meta JSON,
    folder_id UUID REFERENCES %scope%jasp_folder(id)
);;

-- add folder_id to dataset
DO $$
  BEGIN
    BEGIN
      ALTER TABLE %scope%jasp_dataset ADD COLUMN folder_id UUID REFERENCES %scope%jasp_folder(id);
    EXCEPTION
      WHEN duplicate_column THEN NULL;
    END;
  END;
$$;;

-- trigger on dataset to update modified timestamp
DROP TRIGGER IF EXISTS update_folder_modified ON %scope%jasp_folder;;
CREATE TRIGGER update_folder_modified
  BEFORE UPDATE ON %scope%jasp_folder
    FOR EACH ROW EXECUTE PROCEDURE %scope%update_last_modified();;

-- trigger on dataset to update folder modified when dataset changed
DROP TRIGGER IF EXISTS update_dataset_folder_modified ON %scope%jasp_dataset;;
CREATE TRIGGER update_dataset_folder_modified
  AFTER UPDATE ON %scope%jasp_dataset
    FOR EACH ROW EXECUTE PROCEDURE %scope%update_folder_last_modified();;
