





---------------------
import mysql.connector
import csv
import datetime

# Connect to the database
cnx = mysql.connector.connect(user='username', password='password', host='hostname', database='dbname')
cursor = cnx.cursor()

# Fetch all table names from the validation_table
query = "SELECT table_name FROM validation_table"
cursor.execute(query)
table_names = cursor.fetchall()

# Get current timestamp
now = datetime.datetime.now()
timestamp = now.strftime("%Y-%m-%d_%H-%M-%S")

# Open a CSV file to write the validation results
with open(f'validation_results_{timestamp}.csv', mode='w') as csv_file:
    fieldnames = ['table_name', 'validation_result']
    writer = csv.DictWriter(csv_file, fieldnames=fieldnames)
    writer.writeheader()
    
    # Create a log file
    with open(f'validation_log_{timestamp}.txt', 'w') as log_file:
        # Iterate over each table name
        for table_name in table_names:
            # Fetch the validation SQL query for the table from the validation_table
            query = "SELECT validation_sql FROM validation_table WHERE table_name = %s"
            cursor.execute(query, (table_name,))
            validation_sql = cursor.fetchone()[0]

            # Execute the validation SQL query
            cursor.execute(validation_sql)

            # Fetch the result of the validation query
            result = cursor.fetchall()
            
            # Write the validation results to the CSV file
            writer.writerow({'table_name': table_name, 'validation_result': result})
            print(f'Validation results for table {table_name} written to CSV file.')
            log_file.write(f'{table_name} validated at {now}\n')

        # Close the log file
        log_file.close()

    # Close the CSV file
    csv_file.close()

# Close the cursor and connection
cursor.close()
cnx.close()


--- for files -----

metadata.csv file

Table_Name, Column_Name, Data_Type, Max_Length, Nullable, Validation_SQL, Delimiter
tab1, col1, varchar2, 10, N, SELECT COUNT(*) FROM tab1 WHERE LENGTH(col1) > 10, |
tab1, col2, varchar2, 20, Y, SELECT COUNT(*) FROM tab1 WHERE col2 IS NULL, |
tab2, col1, varchar2, 30, N, SELECT COUNT(*) FROM tab2 WHERE LENGTH(col1) > 30, |

-- Data file tables.csv  (file to validate) --
col1|col2|col3
abc|def|ghijkl
abcdefghij|defghijklmn|opqrstuvwxy
abcdefghijklmnopqrst|defghijklmnopqrstuvwxyzabcdef|ghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwx

---- Validate with python panda -- 
import pandas as pd

# Load the metadata table into a pandas dataframe
metadata = pd.read_csv("metadata.csv")

# Load the table data into a pandas dataframe
df = pd.read_csv("table.csv")

# Loop through the metadata dataframe and check if the data in each column is as per the data type mentioned in the metadata
for index, row in metadata.iterrows():
    column_name = row["column_name"]
    data_type = row["data_type"]

    # Check if the data in the column is as per the data type mentioned in the metadata
    if data_type == "varchar2":
        try:
            df[column_name] = df[column_name].astype(str)
        except ValueError as e:
            print(f"Bad data in column {column_name}: {e}")
    elif data_type == "int":
        try:
            df[column_name] = df[column_name].astype(int)
        except ValueError as e:
            print(f"Bad data in column {column_name}: {e}")
    elif data_type == "float":
        try:
            df[column_name] = df[column_name].astype(float)
        except ValueError as e:
            print(f"Bad data in column {column_name}: {e}")
    # Add more data types as necessary for your specific use case

# Store the result of the validation in a separate dataframe
validation_result = pd.DataFrame(columns=["column_name", "error_message"])

# Write the validation result dataframe to a new file
validation_result.to_csv("validation_result.csv", index=False)





