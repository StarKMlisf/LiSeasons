package com.liseasons.festival;

import com.liseasons.festival.FestivalType.FestivalCategory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 节日系统单元测试
 */
public class FestivalManagerTest {

    @Test
    public void testFestivalTypeEnumeration() {
        FestivalType[] types = FestivalType.values();
        assertEquals(5, types.length, "应该只保留5个默认节日");
    }

    @Test
    public void testDefaultFestivalsAreUnifiedAsServerFestivals() {
        List<FestivalType> serverFestivals = FestivalType.getByCategory(FestivalCategory.SERVER_CUSTOM);
        assertEquals(5, serverFestivals.size(), "默认节日应该统一归类为服务器节日");
        assertTrue(serverFestivals.stream().allMatch(f -> f.getCategory() == FestivalCategory.SERVER_CUSTOM));
    }

    @Test
    public void testLegacyCategoriesAreEmptyByDefault() {
        assertTrue(FestivalType.getByCategory(FestivalCategory.TRADITIONAL).isEmpty(), "不应该默认记录传统节日");
        assertTrue(FestivalType.getByCategory(FestivalCategory.LEGAL_HOLIDAY).isEmpty(), "不应该默认记录法定节日");
        assertTrue(FestivalType.getByCategory(FestivalCategory.GREGORIAN).isEmpty(), "不应该默认记录公历节日");
    }

    @Test
    public void testFestivalDisplayNamesAreChinese() {
        assertEquals("元旦", FestivalType.NEW_YEAR.getDisplayName());
        assertEquals("情人节", FestivalType.VALENTINES_DAY.getDisplayName());
        assertEquals("复活节", FestivalType.EASTER.getDisplayName());
        assertEquals("万圣节", FestivalType.HALLOWEEN.getDisplayName());
        assertEquals("圣诞节", FestivalType.CHRISTMAS.getDisplayName());
    }

    @Test
    public void testFestivalDates() {
        for (FestivalType festival : FestivalType.values()) {
            String date = festival.getLunarDate();
            assertNotNull(date, "节日日期不应该为null");
            assertTrue(date.matches("\\d{2}-\\d{2}"), "节日日期格式应该是MM-DD");
        }
    }

    @Test
    public void testSpecificFestivalDates() {
        assertEquals("01-01", FestivalType.NEW_YEAR.getLunarDate(), "元旦应该是01-01");
        assertEquals("02-14", FestivalType.VALENTINES_DAY.getLunarDate(), "情人节应该是02-14");
        assertEquals("04-20", FestivalType.EASTER.getLunarDate(), "复活节应该是04-20");
        assertEquals("10-31", FestivalType.HALLOWEEN.getLunarDate(), "万圣节应该是10-31");
        assertEquals("12-25", FestivalType.CHRISTMAS.getLunarDate(), "圣诞节应该是12-25");
    }

    @Test
    public void testFestivalCategoryDistribution() {
        int total = FestivalType.getByCategory(FestivalCategory.TRADITIONAL).size()
                + FestivalType.getByCategory(FestivalCategory.LEGAL_HOLIDAY).size()
                + FestivalType.getByCategory(FestivalCategory.GREGORIAN).size()
                + FestivalType.getByCategory(FestivalCategory.SERVER_CUSTOM).size();

        assertEquals(5, total, "所有分类的节日总数应该是5");
    }

    @Test
    public void testFestivalCategoryNamesAreUnified() {
        assertEquals("节日", FestivalCategory.TRADITIONAL.getDisplayName());
        assertEquals("节日", FestivalCategory.LEGAL_HOLIDAY.getDisplayName());
        assertEquals("节日", FestivalCategory.GREGORIAN.getDisplayName());
        assertEquals("节日", FestivalCategory.SERVER_CUSTOM.getDisplayName());
    }
}
