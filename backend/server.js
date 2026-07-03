const express = require('express');
const mongoose = require('mongoose');
const dotenv = require('dotenv');

dotenv.config();

const app = express();
const PORT = process.env.PORT || 3000;

app.use(express.json());

// Conexión a MongoDB Atlas (apuntando a practicadb)
mongoose.connect(process.env.MONGO_URI)
.then(() => console.log('Conexión establecida, base de datos practicaDB'))
.catch((error) => console.error('Error de conexión:', error));

// Esquema flexible para "reloj_data"
// Al poner 'strict: false', MongoDB guardará cualquier JSON que le envíes.
const RelojDataSchema = new mongoose.Schema({
    timestamp: { type: Date, default: Date.now } // Registra automáticamente la hora del servidor
}, { strict: false });

// Forzamos a Mongoose a usar exactamente tu colección 'reloj_data'
const RelojData = mongoose.model('RelojData', RelojDataSchema, 'reloj_data');

// --- ENDPOINT POST: Recibir e Insertar datos ---
app.post('/api/v1/reloj', async (req, res) => {
    try {
        // El cuerpo (body) de la petición contiene todo lo que mande el WearOS
        const datosDelReloj = req.body;

        // Creamos el documento con la información recibida
        const nuevoRegistro = new RelojData(datosDelReloj);

        // Guardamos en la colección reloj_data
        const datosGuardados = await nuevoRegistro.save();

        res.status(201).json({
            success: true,
            message: 'Datos del reloj insertados correctamente',
            insertedId: datosGuardados._id,
            data: datosGuardados
        });

    } catch (error) {
        res.status(500).json({
            success: false,
            message: 'Error al insertar los datos en reloj_data',
            error: error.message
        });
    }
});

app.get('/api/v1/reloj', async (req, res) => {
    try {
        // Obtenemos todos los registros de la colección, ordenados por el más reciente
        const datos = await RelojData.find().sort({ timestamp: -1 });
        res.status(200).json({
            success: true,
            count: datos.length,
            data: datos
        });
    } catch (error) {
        res.status(500).json({
            success: false,
            message: 'Error al consultar los datos',
            error: error.message
        });
    }
});

app.listen(PORT, () => {
    console.log(`Servidor corriendo en http://localhost:${PORT}`);
});
