import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.main.EwmMainServiceApp;
import ru.practicum.ewm.main.category.controller.AdminCategoryController;
import ru.practicum.ewm.main.category.dto.CategoryDto;
import ru.practicum.ewm.main.category.dto.NewCategoryDto;
import ru.practicum.ewm.main.category.service.CategoryService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminCategoryController.class)
@ContextConfiguration(classes = EwmMainServiceApp.class)
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private CategoryService categoryService;

    @Test
    void postAddCategory_ReturnCreatedCategory() throws Exception {
        NewCategoryDto newDto = new NewCategoryDto("Music");
        CategoryDto returned = new CategoryDto(1L, "Music");
        when(categoryService.addCategory(any(NewCategoryDto.class))).thenReturn(returned);

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Music\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Music"));
    }
}
