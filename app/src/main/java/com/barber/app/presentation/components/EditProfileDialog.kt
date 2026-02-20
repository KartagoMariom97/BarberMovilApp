package com.barber.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.text.KeyboardOptions

import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.imePadding

import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun EditProfileDialog(
    nombres: String,
    genero: String,
    email: String,
    telefono: String,
    dni: String,
    isUpdating: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String) -> Unit
) {

    var editNombres by remember { mutableStateOf(nombres) }
    var editGenero by remember { mutableStateOf(genero) }
    var editEmail by remember { mutableStateOf(email) }
    var editTelefono by remember { mutableStateOf(telefono) }
    var editDni by remember { mutableStateOf(dni) }

    val focusManager = LocalFocusManager.current

    Dialog(

        onDismissRequest = { onDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {

        Box(

                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .navigationBarsPadding()
                    .imePadding()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        onDismiss()
                    },
                contentAlignment = Alignment.Center
            ) {

            Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .wrapContentHeight()
                        .padding(16.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { 
                            // 🔥 Consumimos el click para que NO se propague
                        },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                    containerColor = Color.White,
                    )
                ) {

                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    Text(
                        text = "Editar Perfil",
                        style = MaterialTheme.typography.titleLarge
                    )

                    OutlinedTextField(
                        value = editNombres,
                        onValueChange = { editNombres = it },
                        label = { Text("Nombres") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Text("Género")

                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SegmentedButton(
                            selected = editGenero == "Masculino",
                            onClick = { editGenero = "Masculino" },
                            shape = SegmentedButtonDefaults.itemShape(0, 2)
                        ) { Text("Masculino") }

                        SegmentedButton(
                            selected = editGenero == "Femenino",
                            onClick = { editGenero = "Femenino" },
                            shape = SegmentedButtonDefaults.itemShape(1, 2)
                        ) { Text("Femenino") }
                    }

                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = editTelefono,
                        onValueChange = { editTelefono = it },
                        label = { Text("Teléfono") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = editDni,
                        onValueChange = { editDni = it },
                        label = { Text("DNI") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {

                        TextButton(
                            onClick = { onDismiss() }
                        ) {
                            Text("Cancelar")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                onSave(
                                    editNombres,
                                    editGenero,
                                    editEmail,
                                    editTelefono,
                                    editDni
                                )
                            },
                            enabled = !isUpdating
                        ) {
                            Text("Guardar")
                        }
                    }
                }
            }
        }
    }
}