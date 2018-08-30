/*
 * Copyright 2017, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.architecture.blueprints.todoapp.addedittask

import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.eq
import com.example.android.architecture.blueprints.todoapp.anyMockito
import com.example.android.architecture.blueprints.todoapp.data.source.Result
import com.example.android.architecture.blueprints.todoapp.util.runBlockingSilent
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Unconfined
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

/**
 * Unit tests for the implementation of [AddEditTaskPresenter].
 */
class AddEditTaskPresenterTest {

    @Mock private lateinit var tasksRepository: TasksRepository

    @Mock private lateinit var addEditTaskView: AddEditTaskContract.View

    private lateinit var addEditTaskPresenter: AddEditTaskPresenter

    @Before
    fun setupMocksAndView() {
        // Mockito has a very convenient way to inject mocks by using the @Mock annotation. To
        // inject the mocks in the test the initMocks method needs to be called.
        MockitoAnnotations.initMocks(this)

        // The presenter wont't update the view unless it's active.
        `when`(addEditTaskView.isActive).thenReturn(true)
    }

    @Test
    fun createPresenter_setsThePresenterToView() {
        // Get a reference to the class under test
        addEditTaskPresenter = AddEditTaskPresenter(
                null, tasksRepository, addEditTaskView, true, CoroutineScope(Unconfined))

        // Then the presenter is set to the view
        verify(addEditTaskView).presenter = addEditTaskPresenter
    }

    @Test
    fun saveNewTaskToRepository_showsSuccessMessageUi() = runBlockingSilent {
        // Get a reference to the class under test
        addEditTaskPresenter = AddEditTaskPresenter(null, tasksRepository, addEditTaskView, true, CoroutineScope(Unconfined))

        // When the presenter is asked to save a task
        addEditTaskPresenter.saveTask("New Task Title", "Some Task Description")

        // Then a task is saved in the repository and the view updated
        verify(tasksRepository).saveTask(anyMockito<Task>()) // saved to the model
        verify(addEditTaskView).showTasksList() // shown in the UI
    }

    @Test
    fun saveTask_emptyTaskShowsErrorUi() {
        // Get a reference to the class under test
        addEditTaskPresenter = AddEditTaskPresenter(null, tasksRepository, addEditTaskView, true, CoroutineScope(Unconfined))

        // When the presenter is asked to save an empty task
        addEditTaskPresenter.saveTask("", "")

        // Then an empty not error is shown in the UI
        verify(addEditTaskView).showEmptyTaskError()
    }

    @Test
    fun saveExistingTaskToRepository_showsSuccessMessageUi() = runBlockingSilent {
        // Get a reference to the class under test
        addEditTaskPresenter = AddEditTaskPresenter(
                "1", tasksRepository, addEditTaskView, true, CoroutineScope(Unconfined))

        // When the presenter is asked to save an existing task
        addEditTaskPresenter.saveTask("Existing Task Title", "Some Task Description")

        // Then a task is saved in the repository and the view updated
        verify(tasksRepository).saveTask(anyMockito<Task>()) // saved to the model
        verify(addEditTaskView).showTasksList() // shown in the UI
    }

    @Test
    fun populateTask_callsRepoAndUpdatesView() = runBlockingSilent {
        val testTask = Task("TITLE", "DESCRIPTION")
        `when`(tasksRepository.getTask(testTask.id)).thenReturn(Result.Success(testTask))
        // Get a reference to the class under test
        addEditTaskPresenter = AddEditTaskPresenter(testTask.id,
                tasksRepository, addEditTaskView, true, CoroutineScope(Unconfined)).apply {
            // When the presenter is asked to populate an existing task
            populateTask()
        }

        // Then the task repository is queried and the view updated
        verify(tasksRepository).getTask(eq(testTask.id))

        verify(addEditTaskView).setTitle(testTask.title)
        verify(addEditTaskView).setDescription(testTask.description)
        assertThat(addEditTaskPresenter.isDataMissing, `is`(false))
    }
}
