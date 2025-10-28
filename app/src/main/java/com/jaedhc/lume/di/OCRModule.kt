package com.jaedhc.lume.di

import com.jaedhc.lume.data.ocr.MlKitTextRecognizer
import com.jaedhc.lume.domain.parser.ports.CategoryClassifier
import com.jaedhc.lume.domain.parser.ports.CategoryRulesRepo
import com.jaedhc.lume.domain.parser.ports.FieldParser
import com.jaedhc.lume.domain.parser.ports.OcrTextRecognizer
import com.jaedhc.lume.domain.parser.services.DefaultOcrEngine
import com.jaedhc.lume.domain.parser.services.RegexFieldParser
import com.jaedhc.lume.domain.parser.services.RulesCategoryClassifier
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object OCRModule {

    @Provides @Singleton
    fun provideFieldParser(): FieldParser = RegexFieldParser()

    @Provides @Singleton
    fun provideCategoryClassifier(rules: CategoryRulesRepo): CategoryClassifier =
        RulesCategoryClassifier(rules)

    @Provides @Singleton
    fun provideOcrTextRecognizer(): OcrTextRecognizer = MlKitTextRecognizer()

    @Provides @Singleton
    fun provideOcrEngine(
        recognizer: OcrTextRecognizer,
        parser: FieldParser,
        classifier: CategoryClassifier
    ): DefaultOcrEngine = DefaultOcrEngine(recognizer, parser, classifier)

}