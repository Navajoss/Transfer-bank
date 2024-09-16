import socket  # Añade esta línea al inicio del archivo
import threading
import tkinter as tk
from tkinter import filedialog, messagebox
from flask import Flask, request
from threading import Thread
import os
from tqdm import tqdm
from zeroconf import ServiceInfo, Zeroconf

# Variables globales
UPLOAD_FOLDER = ''
server_running = False
app = Flask(__name__)
zeroconf = Zeroconf()
service_info = None

# Configuración del servidor Flask para recibir archivos
@app.route('/upload', methods=['POST'])
def upload_file():
    global UPLOAD_FOLDER
    if 'file' not in request.files:
        return 'No file part', 400
    file = request.files['file']
    if file.filename == '':
        return 'No selected file', 400

    # Guardar el archivo con la barra de progreso
    file_path = os.path.join(UPLOAD_FOLDER, file.filename)
    save_file_with_progress(file, file_path)
    return 'File uploaded successfully', 200

def save_file_with_progress(file, file_path):
    """Guarda un archivo mostrando una barra de progreso."""
    with open(file_path, 'wb') as f:
        file_size = int(request.headers.get('Content-Length', 0))
        progress = tqdm(total=file_size, unit='B', unit_scale=True, desc=file.filename)
        for chunk in file.stream:
            f.write(chunk)
            progress.update(len(chunk))
        progress.close()

# Función para anunciar el servicio en la red local usando Zeroconf
def announce_service():
    global zeroconf, service_info
    desc = {'path': '/upload'}
    service_info = ServiceInfo(
        "_http._tcp.local.",
        "FileUploader._http._tcp.local.",
        addresses=[socket.inet_aton("0.0.0.0")],
        port=5000,
        properties=desc,
        server="file-server.local."
    )
    zeroconf.register_service(service_info)

# Función para iniciar el servidor en un hilo separado
def start_server():
    global server_running
    server_running = True
    announce_service()  # Anunciar el servicio en la red local
    app.run(host='0.0.0.0', port=5000, use_reloader=False)

# Función para detener el servidor y desregistrar el servicio mDNS
def stop_server():
    global server_running, zeroconf, service_info
    if server_running:
        zeroconf.unregister_service(service_info)
        zeroconf.close()
        messagebox.showinfo("Servidor", "El servidor se ha detenido. Cierra la aplicación.")
        server_running = False

# Interfaz gráfica con Tkinter
class ServerGUI(tk.Tk):
    def __init__(self):
        super().__init__()
        self.title("Servidor de Transferencia de Archivos")
        self.geometry("400x300")

        # Carpeta para guardar archivos
        self.folder_label = tk.Label(self, text="Carpeta de destino:")
        self.folder_label.pack(pady=10)
        
        self.folder_path = tk.StringVar()
        self.folder_entry = tk.Entry(self, textvariable=self.folder_path, width=50, state='readonly')
        self.folder_entry.pack(pady=5)
        
        self.select_button = tk.Button(self, text="Seleccionar Carpeta", command=self.select_folder)
        self.select_button.pack(pady=10)

        # Botón para iniciar/detener el servidor
        self.start_button = tk.Button(self, text="Iniciar Transmisión", command=self.toggle_server)
        self.start_button.pack(pady=20)

        # Etiqueta de estado del servidor
        self.status_label = tk.Label(self, text="Estado: Servidor detenido")
        self.status_label.pack(pady=10)

    def select_folder(self):
        """Seleccionar la carpeta donde se guardarán los archivos."""
        folder_selected = filedialog.askdirectory()
        if folder_selected:
            self.folder_path.set(folder_selected)
            global UPLOAD_FOLDER
            UPLOAD_FOLDER = folder_selected

    def toggle_server(self):
        """Inicia o detiene el servidor Flask."""
        if not server_running:
            if UPLOAD_FOLDER == '':
                messagebox.showwarning("Carpeta no seleccionada", "Por favor selecciona una carpeta para guardar los archivos.")
                return
            self.start_button.config(text="Detener Transmisión")
            self.status_label.config(text="Estado: Servidor ejecutándose")
            thread = Thread(target=start_server, daemon=True)
            thread.start()
        else:
            stop_server()
            self.start_button.config(text="Iniciar Transmisión")
            self.status_label.config(text="Estado: Servidor detenido")

# Crear y ejecutar la interfaz gráfica
if __name__ == "__main__":
    gui = ServerGUI()
    gui.mainloop()
